package servidorudp;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author jessica
 */
public class Jogador {

    ArrayList<Peca> pecasDoJogador;
    int pontuacao;
    static int cont = 0;
    char equipe;
    int id;
    public InetAddress ip;
    public int porta;
    
    Jogador() {
        this.pecasDoJogador = null;
    }

    Jogador(ArrayList<Peca> pecasDoJogador) {
        this.pecasDoJogador = pecasDoJogador;
    }
    
    Jogador(InetAddress ip, int porta){
        this.ip=ip;
        this.porta=porta;
            System.out.println("Nova conexão com o cliente de IP: " + this.ip + ", " + this.porta);
            this.id=cont;
            
        //se o id for par, vai ser da equipe A
//        if(this.id%2==0)
  //          this.equipe='A';
    //    else
      //      this.equipe='B';
        
        cont ++;
    }


    String imprimirPecasJogador() {
        String listaPecas = "";
        for (Peca peca : this.pecasDoJogador) {
            listaPecas = listaPecas + peca.toString() + ",";
        }
        return listaPecas;
    }

    void removerPeca(Peca p) {
        for (Peca peca : this.pecasDoJogador) {
            if (peca.equals(p)) {
                this.pecasDoJogador.remove(peca);
                break;
            }
        }
    }
    
    boolean contemPeca(Peca p){
   
        for(Peca peca : this.pecasDoJogador ){
            if((peca.parteDireita==p.parteDireita)&&(peca.parteEsquerda==p.parteEsquerda)){
                return true;
                }
        }
         return false;        
    }
}
