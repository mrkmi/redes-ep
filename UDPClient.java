import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
	
	public static void main(String[] args) throws Exception {
		
		int WINDOW_SIZE = 5;
		
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
			
		Scanner sc = new Scanner(System.in);
		System.out.println("Opções:");
		System.out.println("1) Envio normal");
		System.out.println("2) Envio com perda");
		System.out.println("3) Envio fora de ordem");
		System.out.println("4) Envio duplicado");
		System.out.println("5) Envio lento");
		System.out.print("Insira o número da opção escolhida: ");
		int op = sc.nextInt();
		System.out.println("");
		sc.close();
		
		switch(op) {
		case 1:
			System.out.println("Normal\n");
			
			for(int i=0; i<WINDOW_SIZE; i++) {
				/* Define a mensagem a ser enviada */
				byte[] data = new byte[1024];
				String msg = "Bom dia";
				Packet packet = new Packet(i, msg);
				
				/* Serializa o pacote e o transforma em byteArray */
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);   
				out.writeObject(packet);
				data = bos.toByteArray();
				
				/* Envia o pacote */
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
				clientSocket.send(sendPacket);
				System.out.println("Mensagem '" + packet.getMsg()  + "' enviada com ID " + i);
				
				/* Aguarda o recebimente da confirmação do server */
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				clientSocket.receive(recPacket);
				
				/* Deserializa a confirmação do servidor */
				ByteArrayInputStream b = new ByteArrayInputStream(recPacket.getData());
				ObjectInputStream o = new ObjectInputStream(b);
				Ack ack = (Ack) o.readObject();
				System.out.println("Confirmação de mensagem ID: " + ack.getId() + " recebida pelo servidor\n");
			}
			
			break;
		case 2:
			System.out.println("Perda\n");
			
			int lostPacketId = 3;
			boolean lost = true;
			
			for(int i=0; i<WINDOW_SIZE+lostPacketId; i++) {
				
				/* Simular a perda do pacote 3 pulando sua iteração */
				if (i==lostPacketId && lost) {
					lost = false;
					continue;
				}
				else if(i==WINDOW_SIZE && !lost) {
					i = lostPacketId;
					lost = true;
				}
				
				/* Define a mensagem a ser enviada */
				byte[] data = new byte[1024];
				String msg = "Bom dia";
				Packet packet = new Packet(i, msg);
				
				/* Serializa o pacote e o transforma em byteArray */
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);   
				out.writeObject(packet);
				data = bos.toByteArray();
				
				/* Envia o pacote */
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
				clientSocket.send(sendPacket);
				System.out.println("Mensagem '" + packet.getMsg()  + "' enviada com ID " + i);
				
				/* Aguarda o recebimente da confirmação do server */
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				clientSocket.receive(recPacket);
				
				/* Deserializa a confirmação do servidor */
				ByteArrayInputStream b = new ByteArrayInputStream(recPacket.getData());
				ObjectInputStream o = new ObjectInputStream(b);
				Ack ack = (Ack) o.readObject();
				System.out.println("Confirmação de mensagem ID: " + ack.getId() + " recebida pelo servidor\n");
			}
			
			break;
		case 3:
			System.out.println("Fora de Ordem\n");
			
			boolean wrongOrder = true;
			int wrongOrderId = 2;
			
			for(int i=0; i<WINDOW_SIZE+wrongOrderId; i++) {
				/* Define a mensagem a ser enviada */
				byte[] data = new byte[1024];
				String msg = "Bom dia";
				
				Packet packet = new Packet(i, msg);
				
				/* Força o pacote ID:1 ir duplicado */
				if(wrongOrder && i == WINDOW_SIZE - 1) {
					i = wrongOrderId - 1;
					wrongOrder = false;
				}
				
				if(wrongOrder) {
					if(i==wrongOrderId) {
						packet.setId(wrongOrderId + 1);
					}
					else if(i==wrongOrderId + 1) {
						packet.setId(wrongOrderId);
					}
				}
				
				/* Serializa o pacote e o transforma em byteArray */
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);   
				out.writeObject(packet);
				data = bos.toByteArray();
				
				/* Envia o pacote */
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
				clientSocket.send(sendPacket);
				
				if(wrongOrder && ( i==wrongOrderId || i==wrongOrderId + 1)) {
					System.out.println("Mensagem '" + packet.getMsg()  + "' enviada de fora de ordem com ID " + packet.getId());
				}
				else {
					System.out.println("Mensagem '" + packet.getMsg()  + "' enviada com ID " + packet.getId());
				}
				
				/* Aguarda o recebimente da confirmação do server */
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				clientSocket.receive(recPacket);
				
				/* Deserializa a confirmação do servidor */
				ByteArrayInputStream b = new ByteArrayInputStream(recPacket.getData());
				ObjectInputStream o = new ObjectInputStream(b);
				Ack ack = (Ack) o.readObject();
				System.out.println("Confirmação de mensagem ID: " + ack.getId() + " recebida pelo servidor\n");
			}
			
			break;
		case 4:
			System.out.println("Duplicado\n");
			
			boolean duplicate = true;
			
			for(int i=0; i<WINDOW_SIZE; i++) {
				/* Define a mensagem a ser enviada */
				byte[] data = new byte[1024];
				String msg = "Bom dia";
				
				Packet packet = new Packet(i, msg);
				
				/* Força o pacote ID:1 ir duplicado */
				if(i==2 && duplicate) {
					packet.setId(1);
					i--;
					duplicate = false;
				}
				
				/* Serializa o pacote e o transforma em byteArray */
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);   
				out.writeObject(packet);
				data = bos.toByteArray();
				
				/* Envia o pacote */
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
				clientSocket.send(sendPacket);
				
				if(i==1 && !duplicate) {
					System.out.println("Mensagem '" + packet.getMsg()  + "' enviada de forma duplicada com ID " + "1");
				}
				else {
					System.out.println("Mensagem '" + packet.getMsg()  + "' enviada com ID " + i);
				}
				
				/* Aguarda o recebimente da confirmação do server */
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				clientSocket.receive(recPacket);
				
				/* Deserializa a confirmação do servidor */
				ByteArrayInputStream b = new ByteArrayInputStream(recPacket.getData());
				ObjectInputStream o = new ObjectInputStream(b);
				Ack ack = (Ack) o.readObject();
				System.out.println("Confirmação de mensagem ID: " + ack.getId() + " recebida pelo servidor\n");
			}
			clientSocket.close();
			
			break;
		case 5:
			System.out.println("Lento\n");
			
			
			
			break;
		}
	}
}
