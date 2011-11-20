package edu.ufl.cise.cn.peer2peer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import com.test.Peer2;

import edu.ufl.cise.cn.peer2peer.entities.Peer2PeerMessage;
import edu.ufl.cise.cn.peer2peer.entities.PeerMessage;
import edu.ufl.cise.cn.peer2peer.entities.Piece;
import edu.ufl.cise.cn.peer2peer.filehandler.PieceManager;
import edu.ufl.cise.cn.peer2peer.messagehandler.MessageManager;
import edu.ufl.cise.cn.peer2peer.peerhandler.PeerHandler;
import edu.ufl.cise.cn.peer2peer.utility.Constants;
import edu.ufl.cise.cn.peer2peer.utility.LogFactory;
import edu.ufl.cise.cn.peer2peer.utility.MessageLogger;
import edu.ufl.cise.cn.peer2peer.utility.PeerConfigFileReader;
import edu.ufl.cise.cn.peer2peer.utility.PeerInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class Controller.
 *
 * @author sagar
 */
public class Controller {
	
	public static String LOGGER_PREFIX = Controller.class.getCanonicalName();
	
	MessageLogger logger = null;
	/** The controller. */
	private static Controller controller = null;
	
	/** The neighbor peer handler list. */
	private ArrayList<PeerHandler> neighborPeerHandlerList = null;
	
	/** The message manager. */
	private MessageManager messageManager = null;
	
	/** The piece manager. */
	private PieceManager pieceManager = null;
	
	/** The peer configuration reader. */
	private PeerConfigFileReader peerConfigurationReader = null;
	
	/** The peer id. */
	private String peerID;
	
	/**
	 * Gets the single instance of Controller.
	 *
	 * @param peerID the peer id
	 * @return single instance of Controller
	 */
	public static synchronized Controller getInstance(String peerID){
		if(controller == null){
			controller = new  Controller();
			controller.peerID = peerID;
			boolean isInitialized = controller.init();
			
			if(isInitialized == false){
				controller.close();
				controller = null;
			}
		}
		return controller;
	}
	
	/**
	 * Start P2P process.
	 */
	public void startProcess(){
		System.out.println(LOGGER_PREFIX+": Starting Server process");
		startServerThread(peerID);
		System.out.println(LOGGER_PREFIX+": Server process started.");
		System.out.println(LOGGER_PREFIX+": Connecting to client mentioned above the list.");
		connectToPreviousPeerneighbors();
	}
	
	/**
	 * Start peer server thread which will accept connection from other peers and initiates P2P process with them.
	 *
	 * @param peerID the peer id
	 */
	private void startServerThread(String peerID){
		PeerServer peerServer = PeerServer.getInstance(peerID, this);
		new Thread(peerServer).start();
	}
	
	/**
	 * Connect to previous peer neighbors as per the project requirement.
	 */
	private void connectToPreviousPeerneighbors(){
		HashMap<String, PeerInfo> neighborPeerMap = peerConfigurationReader.getPeerInfoMap();
		Set<String> peerIDList = neighborPeerMap.keySet();
		
		
		System.out.println("Current Peer Name : "+peerID);
		
		for (String neighborPeerID : peerIDList) {
			System.out.println("Checking neighbor client : "+neighborPeerID);
			// if peer ID is less than the ID of this peer then it ocured previously in file. 
			if(Integer.parseInt(neighborPeerID) < Integer.parseInt(peerID)){
				System.out.println("Connecting neighbor client : "+neighborPeerID);
				makeConnectionToneighborPeer(neighborPeerMap.get(neighborPeerID));
			}
		}
	}
	
	/**
	 * Make connection to neighbor peer.
	 *
	 * @param peerInfo the peer info
	 */
	private void makeConnectionToneighborPeer(PeerInfo peerInfo){
		String neighborPeerHost = peerInfo.getHostAddress();
		int neighborPortNumber = peerInfo.getPortNumber();
		
		try {
			
			System.out.println(LOGGER_PREFIX+" Connection peer "+peerInfo.getPeerID() + " on "+neighborPeerHost + " port: "+neighborPortNumber);
			
			Socket neighborPeerSocket = new Socket(neighborPeerHost, neighborPortNumber);
			
			System.out.println(LOGGER_PREFIX+" Connected to peer "+peerInfo.getPeerID() + " on "+neighborPeerHost + " port: "+neighborPortNumber);
			
			PeerHandler neighborPeerHandler = PeerHandler.getInstance(neighborPeerSocket, this);
			
			neighborPeerHandler.setPeerID(peerInfo.getPeerID());

			
			neighborPeerHandlerList.add(neighborPeerHandler);
			
			new Thread(neighborPeerHandler).start();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Inits the.
	 *
	 * @return true, if successful
	 */
	private boolean init(){
		
		peerConfigurationReader = PeerConfigFileReader.getInstance();
		
		if(peerConfigurationReader == null){
			return false;
		}
		
		messageManager = MessageManager.getInstance();
		if(messageManager == null){
			return false;
		}
		
		if(PeerConfigFileReader.getInstance().getPeerInfoMap().get(peerID).isFileExists() == false){
			pieceManager = PieceManager.getPieceManagerInstance(false);
		}else{
			pieceManager = PieceManager.getPieceManagerInstance(true);
		}
		

		
		if(pieceManager == null){
			return false;
		}
		
		neighborPeerHandlerList = new ArrayList<PeerHandler>();
		
		logger = LogFactory.getLogger(peerID);
		if(logger == null){
			System.out.println("Unable to Initialize logger object");
			close();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Close.
	 */
	public void close(){
		
	}
	
	/**
	 * Checks if is operation compelete.
	 *
	 * @return true, if is operation compelete
	 */
	public boolean isOperationCompelete(){
		return false;
	}	

	/**
	 * Register peer.
	 *
	 * @param neighborPeerHandler the neighbor peer handler
	 */
	public synchronized void registerPeer(PeerHandler neighborPeerHandler) {
		// TODO Auto-generated method stub
		neighborPeerHandlerList.add(neighborPeerHandler);
	}

	/**
	 * Gets the handshake message.
	 *
	 * @param peerID the peer id
	 * @return the handshake message
	 */
	public synchronized byte[] getHandshakeMessage(String peerID) {
//		messageManager.get
		// TODO Auto-generated method stub
		return null;
	}

	/*public byte[] getBitFieldMessage() {
		int[] missingPieceArray = pieceManager.getMissingPieceNumberArray();
		
		// create bitFieldArray according to number of Missing Piece 
		byte[] bitFieldArray = new byte[10];
		
		// return the bitfield array
		return bitFieldArray;
	}*/
	
	/*public synchronized Peer2PeerMessage getBitFieldMessage() {
		
		Peer2PeerMessage message = Peer2PeerMessage.getInstance(); 
		
		int arr[] = pieceManager.getMissingPieceNumberArray();
		
		ByteBuffer buffer = ByteBuffer.allocate(arr.length*4);
		
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		for(int i=0 ; i<arr.length ; i++){
			buffer.putInt(arr[i]);
		}
		
		byte byteArr[]  = buffer.array();
		
		message.setMessgageType(Constants.BITFIELD_MESSAGE);
		message.setMessageLength(5);
		
		Piece piece = new Piece(byteArr.length);
		piece.setData(byteArr);
		
		message.setData(piece);
		
		return message;
	}*/
	
	
	public synchronized Peer2PeerMessage getBitFieldMessage() {
		
		Peer2PeerMessage message = Peer2PeerMessage.getInstance(); 
		
		message.setHandler(pieceManager.getBitFieldHandler());
		message.setMessgageType(Constants.BITFIELD_MESSAGE);
		

		return message;
	}
	
	public HashMap<String,Double> getSpeedForAllPeers(){

		//------------ Test code
		HashMap<String, Double> peerSpeeds = new HashMap();
		peerSpeeds.put("1010", 100.98d);
		peerSpeeds.put("1014", 120.9d);
		peerSpeeds.put("1015", 98.2d);
		peerSpeeds.put("1016", 78.3d);
		peerSpeeds.put("1017", 108.4d);
		peerSpeeds.put("1019", 101.7d);			
		//System.out.println("Ghanta...tumhare pappa ne bhi li thi speed measure kabhi??");		
		return peerSpeeds;
		//------------ Test code ends
		//return null;
	}
	//sends choke message to all peers in this peerList
	public void chokePeers(ArrayList<String> peerList){
		Peer2PeerMessage chokeMessage = Peer2PeerMessage.getInstance();
		chokeMessage.setMessgageType(Constants.CHOKE_MESSAGE);
		int i = 0, j = 0;
		while(i < peerList.size())
		{
			String peerIDtoChoke = peerList.get(i);
			j = 0;
			while(j < neighborPeerHandlerList.size())
			{
				PeerHandler peer = neighborPeerHandlerList.get(j);
				if(peerIDtoChoke.equals(peer.getPeerId()))
				{
					peer.sendChokeMessage(chokeMessage);
				}
				j++;
			}
			i++;
		}		
		//System.out.println("Sagar beta...choke karo");
	}
	
	public void unChokePeers(ArrayList<String> peerList){
		Peer2PeerMessage unchokeMessage = Peer2PeerMessage.getInstance();
		unchokeMessage.setMessgageType(Constants.UNCHOKE_MESSAGE);
		int i = 0, j = 0;
		while(i < peerList.size())
		{
			String peerIDtoUnchoke = peerList.get(i);
			j = 0;
			while(j < neighborPeerHandlerList.size())
			{
				PeerHandler peer = neighborPeerHandlerList.get(j);
				if(peerIDtoUnchoke.equals(peer.getPeerId()))
				{
					peer.sendUnchokeMessage(unchokeMessage);
				}
				j++;
			}
			i++;
		}		
		System.out.println("Sagar beta...unchoke karo");
	}
	
	public void optimisticallyUnChokePeers(String peer){
		Peer2PeerMessage unchokeMessage = Peer2PeerMessage.getInstance();
		unchokeMessage.setMessgageType(Constants.UNCHOKE_MESSAGE);
		int i = 0;
		PeerHandler peerToHandle;
		while(i < neighborPeerHandlerList.size())
		{
			peerToHandle = neighborPeerHandlerList.get(i);
			if(peer.equals(peerToHandle.getPeerId()))
			{
				peerToHandle.sendUnchokeMessage(unchokeMessage);
			}
			i++;
		}
		//System.out.println("Sagar beta...optimistically unchoke karo :P");
	}
	
	public ArrayList<String> getChokedPeerList(){

		ArrayList<String> chokedPeerList = new ArrayList<String>();
		int i = 0;
		while(i < neighborPeerHandlerList.size())
		{
			PeerHandler peer = neighborPeerHandlerList.get(i);
			if(peer.isPeerChoked())
			{
				chokedPeerList.add(peer.getPeerId());
			}
			i++;
		}
		
		return chokedPeerList;		
		//----------Test code
		/*ArrayList<String> chokedPeers = new ArrayList();
		chokedPeers.add("1002");
		chokedPeers.add("3002");
		chokedPeers.add("4002");
		chokedPeers.add("5002");
		chokedPeers.add("6002");
		chokedPeers.add("7002");
		return chokedPeers;*/
		//---Test code ends
		//return null;
	}
	
	public void insertPiece(Peer2PeerMessage pieceMessage){
		// 
	}
	
	public int[] getMissingPieceIndexArray(){
		return pieceManager.getMissingPieceNumberArray();
	}
}