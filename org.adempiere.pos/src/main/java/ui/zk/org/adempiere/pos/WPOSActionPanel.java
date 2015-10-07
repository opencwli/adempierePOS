package org.adempiere.pos;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.adempiere.pos.search.WPosQuery;
import org.adempiere.pos.search.WQueryBPartner;
import org.adempiere.pos.search.WQueryTicket;
import org.adempiere.pos.service.I_POSPanel;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.MBPartnerInfo;
import org.compiere.model.MOrder;
import org.compiere.model.MPOSKey;
import org.compiere.model.MSequence;
import org.compiere.pos.PosKeyListener;
import org.compiere.print.ReportCtl;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.North;
import org.zkoss.zul.Space;

public class WPOSActionPanel extends WPosSubPanel implements PosKeyListener, I_POSPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2131406504920855582L;
	
	/**
	 * 	Constructor
	 *	@param posPanel POS Panel
	 */
	public WPOSActionPanel (WPOS posPanel) {
		super (posPanel);
	}	//	WPOSActionPanel

	private Button 			f_History;
	private	WPosTextField	f_Name;
	private Button 			f_bNew;
	private Button 			f_Collect;

	private Button			f_bBPartner;
	private Label 			bpartner;
	private Button 			f_logout;
	private Button 			f_Cancel;
	private Button 			f_Next;
	private Button 			f_Back;

	private final String ACTION_BPARTNER    = "BPartner";
	private final String ACTION_LOGOUT      = "Cancel";
	private final String ACTION_CANCEL      = "End";
	private final String ACTION_HISTORY     = "History";
	private final String ACTION_NEW         = "New";
	private final String ACTION_PAYMENT     = "Payment";

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(WPOSActionPanel.class);
	
	private int cont;
	
	@Override
	public void init() {

		Panel parameterPanel = new Panel();
		Borderlayout detailPanel = new Borderlayout();
		Grid parameterLayout = GridFactory.newGridLayout();
		Borderlayout fullPanel = new Borderlayout();
		Grid LayoutButton = GridFactory.newGridLayout();
		Rows rows = null;
		Row row = null;	
		North north = new North();
		cont=0;

		north.setStyle("border: none; width:60%");
		north.setZindex(0);
		fullPanel.appendChild(north);
		parameterPanel.appendChild(parameterLayout);
		parameterLayout.setWidth("60%");
		north.appendChild(parameterPanel);
		rows = parameterLayout.newRows();
		row = rows.newRow();
		Center center = new Center();
		center.setStyle("border: none; width:400px");
		appendChild(center);
		center.appendChild(detailPanel);
		north = new North();
		north.setStyle("border: none");
		detailPanel.setHeight("45%");
		detailPanel.setWidth("50%");
		detailPanel.appendChild(north);
		
		north.appendChild(LayoutButton);
		LayoutButton.setWidth("100%");
		LayoutButton.setHeight("100%");
		rows = LayoutButton.newRows();
		LayoutButton.setStyle("border:none");
		row = rows.newRow();
		row.setHeight("55px");

		row.appendChild(new Space());
		// NEW
		f_bNew = createButtonAction(ACTION_NEW, KeyStroke.getKeyStroke(KeyEvent.VK_F2, Event.F2));
		f_bNew.addActionListener(this);
		row.appendChild(f_bNew);

		// BPartner Search
		f_bBPartner = createButtonAction(ACTION_BPARTNER, KeyStroke.getKeyStroke(KeyEvent.VK_F3, Event.F3));
		f_bBPartner.addActionListener(this);
		f_bBPartner.setTooltiptext(Msg.translate(m_ctx, "IsCustomer"));
		row.appendChild(f_bBPartner);
				
		// HISTORY
		f_History = createButtonAction(ACTION_HISTORY, null);
		f_History.addActionListener(this);
		row.appendChild(f_History); 

		f_Back = createButtonAction("Parent", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		f_Back.setTooltiptext(Msg.translate(m_ctx, "Previous"));
		row.appendChild (f_Back);
		f_Next = createButtonAction("Detail", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		f_Next.setTooltiptext(Msg.translate(m_ctx, "Next"));
		row.appendChild (f_Next);
		
		// PAYMENT
		f_Collect = createButtonAction(ACTION_PAYMENT, null);
		f_Collect.addActionListener(this);
		row.appendChild(f_Collect); 
		f_Collect.setEnabled(false);

		// Cancel
		f_Cancel = createButtonAction (ACTION_CANCEL, null);
		f_Cancel.addActionListener(this);
		f_Cancel.setTooltiptext(Msg.translate(m_ctx, "POS.IsCancel"));
		row.appendChild (f_Cancel);
		f_Cancel.setEnabled(false);
		
		// LOGOUT
		f_logout = createButtonAction (ACTION_LOGOUT, null);
		f_logout.addActionListener(this);
		f_logout.setTooltiptext(Msg.translate(m_ctx, "End"));
		row.appendChild (f_logout);
		row.appendChild(new Space());
		
		row = rows.newRow();
		row.setSpans("1,7");
		row.setHeight("55px");
		// BP
		bpartner = new Label(Msg.translate(Env.getCtx(), "IsCustomer"));
		row.appendChild (new Space());

		
		f_Name = new WPosTextField(v_POSPanel, p_pos.getOSK_KeyLayout_ID());
		f_Name.setHeight("35px");
		f_Name.setStyle("Font-size:medium; font-weight:700");
		f_Name.setWidth("100%");
		f_Name.setValue(bpartner.getValue());
		f_Name.addEventListener(Events.ON_FOCUS, this);
		row.appendChild  (f_Name);
		enableButton();
		
		WPOSInfoProduct panel = new WPOSInfoProduct(v_POSPanel);
		row = rows.newRow();
		row.setSpans("9");
		row.appendChild(panel.getPanel());
		//	List Orders
		v_POSPanel.listOrder();
	}
	/**
	 * 	Print Ticket
	 *  @author Raul Muñoz raulmunozn@gmail.com 
	 */
	public void printTicket()
	{
		if ( v_POSPanel.getM_Order()  == null )
			return;
		
		MOrder order = v_POSPanel.getM_Order();
		
		if (order != null)
		{
			try 
			{
				//print standard document
				if (p_pos.getAD_Sequence_ID() != 0)
				{
					MSequence seq = new MSequence(Env.getCtx(), p_pos.getAD_Sequence_ID(), order.get_TrxName());
					String docno = seq.getPrefix() + seq.getCurrentNext();
					String q = "Confirmar el número consecutivo "  + docno;
					if (FDialog.ask(0, null, q))						
					{
						order.setPOReference(docno);
						order.saveEx();
						ReportCtl.startDocumentPrint(0, order.getC_Order_ID(), false);
						int next = seq.getCurrentNext() + seq.getIncrementNo();
						seq.setCurrentNext(next);
						seq.saveEx();
					}
				}
				else
					ReportCtl.startDocumentPrint(0, order.getC_Order_ID(), false);				
			}
			catch (Exception e) 
			{
				log.severe("PrintTicket - Error Printing Ticket");
			}
		}	  
	}

	/**
	 * Execute deleting an order
	 * If the order is in drafted status -> ask to delete it
	 * If the order is in completed status -> ask to void it it
	 * Otherwise, it must be done outside this class.
	 */
	private void deleteOrder() {
		String errorMsg = null;
		String askMsg = "POS.DeleteOrder";	//	TODO Translate it: Do you want to delete Order?
		if (v_POSPanel.isCompleted()) {	
			askMsg = "POS.OrderIsAlreadyCompleted";	//	TODO Translate it: The order is already completed. Do you want to void it?
		}
		//	Show Ask
		if (FDialog.ask(0, this, Msg.getMsg(m_ctx, askMsg))) {
			errorMsg = v_POSPanel.cancelOrder();
		} 
		if(errorMsg != null){
			FDialog.error(0,  Msg.parseTranslation(m_ctx, errorMsg));
			return;
		}
		//	Update
		v_POSPanel.refreshPanel();
	} // deleteOrder
	
	/**
	 * 
	 */
	private void payOrder() {

		//Check if order is completed, if so, print and open drawer, create an empty order and set cashGiven to zero
		if( v_POSPanel.getM_Order() == null ) {
				FDialog.warn(0, Msg.getMsg(m_ctx, "You must create an Order first"));
				return;
		}
		WCollect collect = new WCollect(v_POSPanel);
		if (collect.showCollect()) {
			printTicket();
			v_POSPanel.setOrder(0);
		}
	}
	/**
	 * 	Find/Set BPartner
	 */
	private void findBPartner()
	{
		String query = f_Name.getText();
		//	
		if (query == null || query.length() == 0)
			return;
		
		// unchanged
		if (v_POSPanel.hasBPartner() 
				&& v_POSPanel.compareBPName(query))
			return;
		
		query = query.toUpperCase();
		//	Test Number
		boolean allNumber = true;
		boolean noNumber = true;
		char[] qq = query.toCharArray();
		for (int i = 0; i < qq.length; i++) {
			if (Character.isDigit(qq[i])) {
				noNumber = false;
				break;
			}
		} try {
			Integer.parseInt(query);
		} catch (Exception e) {
			allNumber = false;
		}
		String Value = query;
		String Name = (allNumber ? null : query);
		String EMail = (query.indexOf('@') != -1 ? query : null); 
		String Phone = (noNumber ? null : query);
		String City = null;
		//
		MBPartnerInfo[] results = MBPartnerInfo.find(m_ctx, Value, Name, 
			/*Contact, */null, EMail, Phone, City);
		
		//	Set Result
		if (results.length == 1) {
			v_POSPanel.setC_BPartner_ID(v_POSPanel.getC_BPartner_ID());
			f_Name.setText(v_POSPanel.getBPName());
		} else {	//	more than one
			changeBusinessPartner(results);
		}

	}	//	findBPartner
	
	/**
	 * 	Change in Order the Business Partner, including Price list and location
	 *  In Order and POS
	 *  @param results
	 */
	public void changeBusinessPartner(MBPartnerInfo[] results) {
		// Change to another BPartner
		WQueryBPartner qt = new WQueryBPartner(v_POSPanel);
		qt.setResults(results);
		AEnv.showWindow(qt);
		if (qt.getRecord_ID() > 0) {
			f_Name.setText(v_POSPanel.getBPName());
			if(!v_POSPanel.hasOrder()) {
				v_POSPanel.newOrder(qt.getRecord_ID());
				v_POSPanel.refreshPanel();
			} else {
//				v_POSPanel.setC_BPartner_ID(qt.getRecord_ID());
			}
			log.fine("C_BPartner_ID=" + qt.getRecord_ID());
		}	
	}	
	public boolean showKeyboard(WPosTextField field, Label label) {
		if(field.getText().equals(label.getValue()))
			field.setValue("");
		WPOSKeyboard keyboard =  v_POSPanel.getKeyboard(field.getKeyLayoutId()); 
		keyboard.setWidth("750px");
		keyboard.setHeight("380px");
		keyboard.setPosTextField(field);	
		AEnv.showWindow(keyboard);
		if(field.getText().equals("")) 
			field.setValue(label.getValue());
		return keyboard.isCancel();
	}
	
	@Override
	public void onEvent(org.zkoss.zk.ui.event.Event e) throws Exception {
		cont++;
		if(e.getName().equals(Events.ON_FOCUS)) {
			if(cont<2){
				if (e.getTarget().equals(f_Name)) {
					if(e.getTarget().equals(f_Name)) {
						if(!showKeyboard(f_Name,bpartner))
							findBPartner(); 
					}
				}
			}else {
				cont=0;
				f_bBPartner.setFocus(true);
			}
		}
		if (e.getTarget().equals(f_bNew)){
			v_POSPanel.newOrder();
			v_POSPanel.refreshPanel();
			e.stopPropagation();
				return;
		}
		else if(e.getTarget().equals(f_Collect)){
			payOrder();
			return;
		}
		else if (e.getTarget().equals(f_Back) ){
			previousRecord();
		}
		else if (e.getTarget().equals(f_Next) ){
			nextRecord();
		}
		else if(e.getTarget().equals(f_logout)){
			dispose();
			return;
		}
		else if (e.getTarget().equals(f_bBPartner)) {
			WQueryBPartner qt = new WQueryBPartner(v_POSPanel);
			AEnv.showWindow(qt);
			findBPartner();
		}
		// Cancel
		else if (e.getTarget().equals(f_Cancel)){
			deleteOrder();
		}
		//	History
		if (e.getTarget().equals(f_History)) {
			
			WPosQuery qt = new WQueryTicket(v_POSPanel);
			qt.setVisible(true);
			AEnv.showWindow(qt);
//			if (qt.getRecord_ID() > 0) {
//				v_POSPanel.setOrder(qt.getRecord_ID());
//				v_POSPanel.reloadIndex(qt.getRecord_ID());
//			} else {
//				return;
//			}
		}
		v_POSPanel.refreshPanel();

	}

	@Override
	public void refreshPanel() {
		f_Name.setText(v_POSPanel.getBPName());
	}

	@Override
	public String validatePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Previous Record Order
	 */
	public void previousRecord() {
		v_POSPanel.previousRecord();
		//	Refresh
		v_POSPanel.refreshPanel();
	}

	/**
	 * Next Record Order
	 */
	public void nextRecord() {
		v_POSPanel.nextRecord();
		//	Refresh
		v_POSPanel.refreshPanel();
	}
	
	@Override
	public void changeViewPanel() {
		if (v_POSPanel != null )
		{
			MOrder order = v_POSPanel.getM_Order();
			if (order != null)
			{	
				if(v_POSPanel.getC_BPartner_ID() <= 0) {
					f_Name.setText(bpartner.getValue());
				}
  					f_bBPartner.setEnabled(!v_POSPanel.isCompleted());
  					f_Name.setEnabled(!v_POSPanel.isCompleted());
  					//	For Next
  					f_Next.setEnabled(!v_POSPanel.isLastRecord());
  					//	For Back
  					f_Back.setEnabled(!v_POSPanel.isFirstRecord());
  					
  			    // Button New: enabled when lines existing or order is voided
  				f_bNew.setEnabled(order.getLines().length != 0 || order.getDocStatus().equals(MOrder.DOCSTATUS_Voided));
  				

  			    // History Button: enabled when lines existing or order is voided
  				if(order.getLines().length != 0 || order.getDocStatus().equals(MOrder.DOCSTATUS_Voided))
  	  				f_History.setEnabled(true);  	
  				else
  					f_History.setEnabled(false);
  				//  For Collect
  				if(v_POSPanel.hasLines()
  						&& !v_POSPanel.isPaid()) {
  					//	For Credit Order
  					f_Collect.setEnabled(true);
  				} else {
  					f_Collect.setEnabled(false);
  				}
  				//	For Cancel Action
  				f_Cancel.setEnabled(!v_POSPanel.isVoided());

			} else {
				f_bNew.setEnabled(true);
				f_bBPartner.setEnabled(true);
				f_Name.setEnabled(true);
				f_History.setEnabled(true);
				//	For Next
				f_Next.setEnabled(!v_POSPanel.isLastRecord());
				//	For Back
				f_Back.setEnabled(!v_POSPanel.isFirstRecord());
				f_Collect.setEnabled(false);
				//	For Cancel Action
				f_Cancel.setEnabled(false);
			}
			
		}
	}
	
	public void enableButton(){
		f_Name.setText(bpartner.getValue());
		f_bBPartner.setEnabled(false);
		v_POSPanel.setC_BPartner_ID(0);
		f_bNew.setEnabled(true);
		f_Cancel.setEnabled(false);
		f_History.setEnabled(true);
		f_Collect.setEnabled(false);
	}
	
	@Override
	public void keyReturned(MPOSKey key) {
		// TODO Auto-generated method stub
		
	}
	
	
}