package servidorudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author jessica
 */
public class ServidorUDP {

    public ArrayList<Jogador> jogadores;
    public ArrayList<Jogador> jogadoresConectados;
    ControladorJogo c;
    int pontuacaoPartida = 0;
    int pontuacaoEquipeA = 0;
    int pontuacaoEquipeB = 0;
    int pontuacaoJogo = 0;
    int quantidadeJogadores = 0;
    DatagramSocket socketServidor;

    ServidorUDP() throws IOException {
        socketServidor = new DatagramSocket(40000);
        jogadoresConectados = new ArrayList();
        try {        
            while (true) {
                byte[] mensagemRecebida = new byte[1024];
                DatagramPacket datagramaRecebido = new DatagramPacket(mensagemRecebida, mensagemRecebida.length);
                socketServidor.receive(datagramaRecebido);
                System.out.println("recebeu"+new String(datagramaRecebido.getData()));
                this.quantidadeJogadores++;
                if (this.quantidadeJogadores < 5) {
                    InetAddress ip= datagramaRecebido.getAddress();
                    int porta = datagramaRecebido.getPort();
                    jogadoresConectados.add(new Jogador(ip, porta));
                    if (jogadoresConectados.size() == 4) {
                        this.c = new ControladorJogo(jogadoresConectados);
                        jogadores = c.getJogadoresOrdenados();
                        iniciarJogo();
                    }
                } //else {
                    //datagrama.close();
                //}
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro:" + ex.getMessage());
            System.out.println("erro"+ex.getMessage());
        }
    }

    private void iniciarJogo() throws IOException {
        enviarMensagemInicial(this.c);
        
        //TO DO: 
        // 1) reiniciar partida ap�s vitoria de um participante
        while (true) {
            if(pontuacaoEquipeA<7 && pontuacaoEquipeB<7)
                iniciarPartida();
        }
    }
    
    public static void main(String[] args) throws Exception {
        new ServidorUDP();
    }

    private void enviarMensagemAoJogador(Jogador j, String mensagem) throws IOException {
    byte[] mensagemEnviada = new byte[1024];
        mensagemEnviada=mensagem.getBytes();
        
        DatagramPacket datagramaParaEnviar= new DatagramPacket(mensagemEnviada, mensagemEnviada.length, j.ip, j.porta);
        System.out.println("enviando.."+new String(datagramaParaEnviar.getData()));
        socketServidor.send(datagramaParaEnviar);
    }

    private void informarQueOJogadorComprouPecas(Jogador jogadorQueComprouPeca, int numeroPecasCompradas) throws IOException {
        for (Jogador j : jogadores) {
            if (j != jogadorQueComprouPeca) {
                enviarMensagemAoJogador(j, TipoMensagem.ID_MENSAGEM_QTD_PECAS_COMPRADAS + "#" + numeroPecasCompradas);
                
            }
        }
    }

    private void informarJogadaParaTodosOsJogadores(String[] itensJogada, Jogador jogadorQueJogouPeca) throws IOException {
        String posicao = itensJogada[0];
        Peca p = new Peca(itensJogada[1]);
        
        //se comprou peças
        if (itensJogada.length > 3) {
            aumentarNumeroDePecasJogador(itensJogada[3], jogadorQueJogouPeca);
            informarQueOJogadorComprouPecas(jogadorQueJogouPeca, itensJogada[3].split(",").length);
        }
        for (Jogador j : jogadores) {
            enviarMensagemAoJogador(j, TipoMensagem.ID_MENSAGEM_INFORMAR_JOGADA + "#" + posicao + "#" + p.toString() + "#" + this.c.quantidadeDePecasJogadores());
        }
    }

    private void informarVitoriaPartidaParaTodosOsJogadores(int idJogador, String[] itensJogada)throws IOException {
        calcularPontuacaoPartida(itensJogada);
        //char equipe='A';
        for (Jogador j : jogadores) {
            //if(j.id==idJogador){
              //  equipe=j.equipe;
          //      if(equipe=='A')
            if(idJogador%2==0)
                    pontuacaoEquipeA+=pontuacaoPartida;
                else
                    pontuacaoEquipeB+=pontuacaoPartida;
            //}
            enviarMensagemAoJogador(j, TipoMensagem.ID_MENSAGEM_VENCEDOR_PARTIDA + "#" + idJogador + "#" + this.pontuacaoPartida);
        }
    }

    public void enviarMensagemInicial(ControladorJogo c) throws IOException {
        for (Jogador j : jogadores) {
            enviarMensagemAoJogador(j, TipoMensagem.ID_MENSAGEM_INICIAL + "#" + j.id + "#" + j
                    .imprimirPecasJogador() + "#" + c.imprimirPecasDisponiveis()+"#"+j.equipe);
        }
    }

    public void informarJogadorDaVez(int idJogadorDaVez) throws IOException {
        for (Jogador j : jogadores) {
            enviarMensagemAoJogador(j, TipoMensagem.ID_MENSAGEM_INFORMAR_JOGADOR_DA_VEZ + "#" + idJogadorDaVez);
        }
    }

    private void aumentarNumeroDePecasJogador(String pecasCompradas, Jogador jogadorQueComprouPecas) {
                    String[] pecas = pecasCompradas.split(",");
                    for (int i = 0; i < pecas.length; i++) {
                        this.c.comprarPeca(jogadorQueComprouPecas, new Peca(pecas[i]));
                    }            
    }

    private void iniciarPartida() throws IOException {
        
            for (int i = 0; i < 4; i = (i + 1) % 4) {
                informarJogadorDaVez(i);
                
                byte[] mensagemRecebida = new byte[1024];
                DatagramPacket datagramaRecebido = new DatagramPacket(mensagemRecebida, mensagemRecebida.length);
                socketServidor.receive(datagramaRecebido);
                String jogada = new String(datagramaRecebido.getData());                 
                String[] itensJogada = jogada.trim().split("#");
                //Se o Jogador jogou peça na mesa
                if (itensJogada.length > 2) {
                    this.c.inserirPecaMesa(new Peca(itensJogada[1]), itensJogada[0], jogadores.get(i));
                    informarJogadaParaTodosOsJogadores(itensJogada, jogadores.get(i));
                    if (jogadores.get(i).pecasDoJogador.isEmpty()) {
                        informarVitoriaPartidaParaTodosOsJogadores(jogadores.get(i).id, itensJogada);
                    }       
                }
                //Se o jogador comprou peças e passou a vez
                 else if (itensJogada.length == 2) {
                    aumentarNumeroDePecasJogador(itensJogada[1], jogadores.get(i));
                    informarQueOJogadorComprouPecas(jogadores.get(i), (itensJogada[1].split(",")).length);
                 }
            
    }}
    
    private int calcularPontuacaoPartida(String[] itensJogada) {
        Peca pecaExtremidadeEsquerda = new Peca(itensJogada[2].split(",")[0]);
        Peca pecaExtremidadeDireita = new Peca(itensJogada[2].split(",")[1]);
        Peca ultimaPeca = new Peca(itensJogada[1]);
        String posicao = itensJogada[0];
        boolean carroca = false;
        boolean serviuParaOsDoisLados = false;
        if (ultimaPeca.parteDireita == ultimaPeca.parteEsquerda) {
            carroca = true;
        }
        if (((posicao.equals("1")) && (ultimaPeca.parteDireita == pecaExtremidadeEsquerda.parteEsquerda)) || ((posicao.equals("0")) && (ultimaPeca.parteEsquerda == pecaExtremidadeDireita.parteDireita))) {
            serviuParaOsDoisLados = true;
        }
        if ((carroca) && (serviuParaOsDoisLados)) {
            this.pontuacaoPartida = 4;
        } else if (serviuParaOsDoisLados) {
            this.pontuacaoPartida = 3;
        } else if (carroca) {
            this.pontuacaoPartida = 2;
        } else {
            this.pontuacaoPartida = 1;
        }
        return pontuacaoPartida;
    }
    
}
