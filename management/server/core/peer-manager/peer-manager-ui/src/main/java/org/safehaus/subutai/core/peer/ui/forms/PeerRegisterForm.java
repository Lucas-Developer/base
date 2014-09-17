package org.safehaus.subutai.core.peer.ui.forms;


import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.ui.PeerUI;

import java.util.List;
import java.util.UUID;


public class PeerRegisterForm extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,
    "movingGuides":false,"snappingDistance":10} */

    @AutoGenerated
    private AbsoluteLayout mainLayout;
    @AutoGenerated
    private AbsoluteLayout absoluteLayout_2;
    @AutoGenerated
    private Table peersTable;
    @AutoGenerated
    private Button showPeersButton;
    @AutoGenerated
    private Button registerButton;
    @AutoGenerated
    private Label ID;
    @AutoGenerated
    private TextField idTextField;
    @AutoGenerated
    private TextField ipTextField;
    @AutoGenerated
    private Label IP;
    @AutoGenerated
    private Label peerRegistration;
    @AutoGenerated
    private Label name;
    @AutoGenerated
    private TextField nameTextField;

    private PeerUI peerUI;


    /**
     * The constructor should first build the main layout, set the composition root and then do any custom
     * initialization.
     * <p/>
     * The constructor will not be automatically regenerated by the visual editor.
     */
    public PeerRegisterForm(final PeerUI peerUI) {
        buildMainLayout();
        setCompositionRoot(mainLayout);

        // TODO add user code here
        this.peerUI = peerUI;
    }


    @AutoGenerated
    private AbsoluteLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new AbsoluteLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");

        // top-level component properties
        setWidth("100.0%");
        setHeight("100.0%");

        // absoluteLayout_2
        absoluteLayout_2 = buildAbsoluteLayout_2();
        mainLayout.addComponent(absoluteLayout_2, "top:20.0px;right:0.0px;bottom:-20.0px;left:0.0px;");

        return mainLayout;
    }


    @AutoGenerated
    private AbsoluteLayout buildAbsoluteLayout_2() {
        // common part: create layout
        absoluteLayout_2 = new AbsoluteLayout();
        absoluteLayout_2.setImmediate(false);
        absoluteLayout_2.setWidth("100.0%");
        absoluteLayout_2.setHeight("100.0%");

        // nameTextField
        nameTextField = new TextField();
        nameTextField.setImmediate(false);
        nameTextField.setWidth("-1px");
        nameTextField.setHeight("-1px");
        nameTextField.setMaxLength(256);
        absoluteLayout_2.addComponent(nameTextField, "top:36.0px;left:100.0px;");

        // name
        name = new Label();
        name.setImmediate(false);
        name.setWidth("-1px");
        name.setHeight("-1px");
        name.setValue("Name");
        absoluteLayout_2.addComponent(name, "top:36.0px;left:20.0px;");

        // peerRegistration
        peerRegistration = new Label();
        peerRegistration.setImmediate(false);
        peerRegistration.setWidth("-1px");
        peerRegistration.setHeight("-1px");
        peerRegistration.setValue("Peer registration");
        absoluteLayout_2.addComponent(peerRegistration, "top:0.0px;left:20.0px;");

        // IP
        IP = new Label();
        IP.setImmediate(false);
        IP.setWidth("-1px");
        IP.setHeight("-1px");
        IP.setValue("IP");
        absoluteLayout_2.addComponent(IP, "top:80.0px;left:20.0px;");

        // ipTextField
        ipTextField = new TextField();
        ipTextField.setImmediate(false);
        ipTextField.setWidth("-1px");
        ipTextField.setHeight("-1px");
        ipTextField.setMaxLength(15);
        absoluteLayout_2.addComponent(ipTextField, "top:80.0px;left:100.0px;");

        // idTextField
        idTextField = new TextField();
        idTextField.setImmediate(false);
        idTextField.setWidth("-1px");
        idTextField.setHeight("-1px");
        idTextField.setMaxLength(64);
        absoluteLayout_2.addComponent(idTextField, "top:120.0px;left:100.0px;");

        // ID
        ID = new Label();
        ID.setImmediate(false);
        ID.setWidth("-1px");
        ID.setHeight("-1px");
        ID.setValue("ID");
        absoluteLayout_2.addComponent(ID, "top:120.0px;left:20.0px;");

        // registerButton
        registerButton = createRegisterButton();


        absoluteLayout_2.addComponent(registerButton, "top:160.0px;left:20.0px;");

        // showPeersButton
        showPeersButton = createShowPeersButton();
        absoluteLayout_2.addComponent(showPeersButton, "top:234.0px;left:20.0px;");

        // peersTable
        peersTable = new Table();
        peersTable.setCaption("Peers");
        peersTable.setImmediate(false);
        peersTable.setWidth("780px");
        peersTable.setHeight("283px");
        absoluteLayout_2.addComponent(peersTable, "top:294.0px;left:20.0px;");

        return absoluteLayout_2;
    }


    private Button createShowPeersButton() {
        showPeersButton = new Button();
        showPeersButton.setCaption("Show peers");
        showPeersButton.setImmediate(false);
        showPeersButton.setWidth("-1px");
        showPeersButton.setHeight("-1px");

        showPeersButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final Button.ClickEvent clickEvent) {
                List<Peer> peers = peerUI.getPeerManager().peers();
                BeanItemContainer<Peer> ds = new BeanItemContainer<Peer>(Peer.class);
                ds.addAll(peers);
                peersTable.setContainerDataSource(ds);
                peersTable.refreshRowCache();
            }
        });

        return showPeersButton;
    }


    private void populateData() {
        List<Peer> peers = peerUI.getPeerManager().peers();
        peersTable.removeAllItems();
        peersTable.addContainerProperty( "UUID", String.class, null );
        peersTable.addContainerProperty( "Name", String.class, null );
        peersTable.addContainerProperty( "IP", String.class, null );
        peersTable.addContainerProperty( "Unregister", Button.class, null );

        for ( final Peer peer : peers )
        {
            Button unregisterButton = new Button( "Unregister" );
            unregisterButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    peerUI.getPeerManager().unregister( peer.getId().toString() );
                }
            } );
            peersTable.addItem( new Object[] { peer.getId(), peer.getName(), peer.getIp(), unregisterButton }, null );
        }
    }


    private Button createRegisterButton() {
        registerButton = new Button();
        registerButton.setCaption( "Register" );
        registerButton.setImmediate( true );
        registerButton.setWidth( "-1px" );
        registerButton.setHeight( "-1px" );

        registerButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                Peer peer = new Peer();
                String name = nameTextField.getValue();
                String ip = ipTextField.getValue();
                String id = idTextField.getValue();
//                peer.setName(name);
//                peer.setIp(ip);
//                peer.setId( UUID.fromString(id));
                peerUI.getPeerManager().register(peer);
                if ( name.length() > 0 && ip.length() > 0 && id.length() > 0 )
                {
                    peer.setName( name );
                    peer.setIp( ip );
                    peer.setId( UUID.fromString(id) );
                    peerUI.getPeerManager().register( peer );
                }
                else
                {
                    Notification.show( "Check form values" );
                }
            }
        } );

        return registerButton;
    }


}