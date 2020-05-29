import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
	
	public static void main(String[] args) throws Exception {
		int buffer = 0;
		int last = 0;
		int WINDOW_SIZE = 5;
			
		DatagramSocket serverSocket = new DatagramSocket(9876);
		
		boolean wrongOrder = false;
		
		while (true) {
			
			/* Aguarda a chegada de um pacote do cliente */
			byte[] recBuffer = new byte[1024];
			DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
			System.out.println("Esperando pacote do cliente...");
			serverSocket.receive(recPacket);
			
			/* Deserializa o pacote recebido */
			ByteArrayInputStream b = new ByteArrayInputStream(recPacket.getData());
			ObjectInputStream o = new ObjectInputStream(b);
			Packet packet = (Packet) o.readObject();
			
			/* Verifica a duplicidade do pacote recebido */
			if(buffer > last && last == packet.getId()) {
				System.out.println("Mensagem ID:" + packet.getId() + " '" + packet.getMsg() + "' recebida de forma duplicada\n");
			}
			else if(packet.getId() > buffer && !wrongOrder) {
				System.out.println("Mensagem ID:" + packet.getId() + " '" + packet.getMsg() + "' recebida fora de ordem\n");
				wrongOrder = true;
			}
			else {
				System.out.println("Mensagem ID:" + packet.getId() + " '" + packet.getMsg() + "' recebida\n");
			}
			
			/* Verifica a confirmação correta para o pacote recebido */
			int ackId = last;
			if(!wrongOrder && buffer == packet.getId()) {
				last = buffer;
				ackId = buffer;
				buffer += 1;							
			}
			/* Serializa o pacote de confirmação adequado */
			byte[] sendBuff = new byte[1024];
			Ack ack = new Ack(ackId);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);   
			out.writeObject(ack);
			sendBuff = bos.toByteArray();
			
			/* Obtem a porta e IP para enviar a confirmação */
			InetAddress IPAddress = recPacket.getAddress();
			int port = recPacket.getPort();
			
			/* Envia o pacote de confirmação para o cliente */
			DatagramPacket sendPacket =  new DatagramPacket(sendBuff, sendBuff.length, IPAddress, port);
			serverSocket.send(sendPacket);
			
			if (wrongOrder && packet.getId() == WINDOW_SIZE-1) {
				wrongOrder = false;
			}
		}
			
	}
}
