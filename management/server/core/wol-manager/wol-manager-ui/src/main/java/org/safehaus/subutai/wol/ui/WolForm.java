package org.safehaus.subutai.wol.ui;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.wol.api.WolManagerException;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.Page;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class WolForm extends CustomComponent implements Disposable
{

    @AutoGenerated
    private Button sendMagicPackageButton;
    @AutoGenerated
    private Button SendMagicPackageListButton;
    @AutoGenerated
    private TextField macIDTextField;
    @AutoGenerated
    private Label macIDLabel;
    @AutoGenerated
    private Label singleOperationLabel;
    @AutoGenerated
    private Label multiOperationLabel;
    @AutoGenerated
    private TextArea macListTextArea;

    @AutoGenerated
    private AbsoluteLayout mainLayout;
    private WolUI wolui;


    public WolForm( WolUI wolui )
    {
        // common part: create layout
        mainLayout = new AbsoluteLayout();
        mainLayout.setImmediate( false );
        mainLayout.setWidth( "100%" );
        mainLayout.setHeight( "100%" );
        this.wolui = wolui;

        // top-level component properties
        setWidth( "100.0%" );
        setHeight( "100.0%" );

        // wolMasterLayout
        final AbsoluteLayout wolMasterLayout = buildAbsoluteLayout_2();
        mainLayout.addComponent( wolMasterLayout, "top:20.0px;right:0.0px;bottom:-20.0px;left:0.0px;" );

        setHeight( 100, Unit.PERCENTAGE );
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        sheet.addTab( mainLayout, "Wake on Lan Operations" );
        verticalLayout.addComponent( sheet );

        setCompositionRoot( verticalLayout );
    }


    @AutoGenerated
    private AbsoluteLayout buildAbsoluteLayout_2()
    {

        // common part: create layout
        AbsoluteLayout absoluteLayout = new AbsoluteLayout();
        absoluteLayout.setImmediate( false );
        absoluteLayout.setWidth( "100.0%" );
        absoluteLayout.setHeight( "100.0%" );

        // singleOperationLabel
        singleOperationLabel = new Label();
        singleOperationLabel.setImmediate( false );
        singleOperationLabel.setWidth( "-1px" );
        singleOperationLabel.setHeight( "-1px" );
        singleOperationLabel.setValue( "Single Wake On Lan Operation" );
        absoluteLayout.addComponent( singleOperationLabel, "top:20.0px;left:60.0px;" );

        // multiOperationLabel
        multiOperationLabel = new Label();
        multiOperationLabel.setImmediate( false );
        multiOperationLabel.setWidth( "-1px" );
        multiOperationLabel.setHeight( "-1px" );
        multiOperationLabel.setValue( "Multi Wake On Lan Operation" );
        absoluteLayout.addComponent( multiOperationLabel, "top:200.0px;left:60.0px;" );

        // MacID label
        //  macIDLabel = new Label();
        //  macIDLabel.setImmediate( false );
        //  macIDLabel.setWidth( "-1px" );
        //  macIDLabel.setHeight( "-1px" );
        //  macIDLabel.setValue( "Mac Address" );
        //  absoluteLayout.addComponent( macIDLabel, "top:65.0px;left:25.0px;" );

        // MacID texfield
        macIDTextField = new TextField();
        macIDTextField.setImmediate( false );
        macIDTextField.setWidth( "-1px" );
        macIDTextField.setHeight( "-1px" );
        macIDTextField.setMaxLength( 17 );
        macIDTextField.setCaption( "Mac Address" );
        absoluteLayout.addComponent( macIDTextField, "top:60.0px;left:60.0px;" );

        // sendMagicPackageButton
        sendMagicPackageButton = createSendMagicPackageButton();
        absoluteLayout.addComponent( sendMagicPackageButton, "top:60.0px;left:250.0px;" );

        // macList TextArea
        macListTextArea = new TextArea( "Mac Address Values" );
        macListTextArea.setRows( 10 );
        absoluteLayout.addComponent( macListTextArea, "top:250.0px;left:60.0px;" );

        // sendMagicPackageListButton
        SendMagicPackageListButton = createSendMagicPackageListButton();
        absoluteLayout.addComponent( SendMagicPackageListButton, "top:400.0px;left:250.0px;" );

        return absoluteLayout;
    }


    // show given message in a notification box
    private void showNotification( String message )
    {
        Notification notification = new Notification( message, Notification.Type.WARNING_MESSAGE );
        notification.setDelayMsec( 1500 );
        notification.show( Page.getCurrent() );
    }


    //Check a given macID is valid or not
    private boolean checkMacID( String macID )
    {
        if ( macID.length() == 17 )
        {
            if ( ( macID.charAt( 2 ) == ':' ) && ( macID.charAt( 5 ) == ':' ) && ( macID.charAt( 8 ) == ':' ) &&
                    ( macID.charAt( 11 ) == ':' ) && ( macID.charAt( 14 ) == ':' ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }


    //check given Mac ArrayList is valid or not
    private boolean checkMacList( ArrayList<String> macList )
    {

        for ( int i = 0; i < macList.size(); i++ )
        {
            if ( !checkMacID( macList.get( i ) ) )
            {
                return false;
            }
        }
        return true;
    }


    //Create SendMagicPackageListButton
    private Button createSendMagicPackageListButton()
    {
        SendMagicPackageListButton = new Button();
        SendMagicPackageListButton.setCaption( "Send List of Magic Packages" );
        SendMagicPackageListButton.setImmediate( false );
        SendMagicPackageListButton.setWidth( "-1px" );
        SendMagicPackageListButton.setHeight( "-1px" );

        SendMagicPackageListButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                String macList = macListTextArea.getValue();
                ArrayList<String> macListArray = new ArrayList<String>();
                BufferedReader bufReader = new BufferedReader( new StringReader( macList ) );
                try
                {
                    String line = null;
                    int i = 0;
                    while ( ( line = bufReader.readLine() ) != null )
                    {
                        macListArray.add( i, line );
                        i++;
                    }

                    if ( checkMacList( macListArray ) )
                    {
                        try
                        {
                            Boolean commandresult = wolui.getWolManager().sendMagicPackageByList( macListArray );
                            if ( commandresult )
                            {
                                showNotification( "All Magic Package Sent to Listed Mac Address" );
                            }
                            else
                            {
                                showNotification( "Operation is  not successfull" );
                            }
                        }
                        catch ( WolManagerException e )
                        {
                            showNotification( e.getMessage() );
                        }
                    }
                    else
                    {
                        showNotification( "Invalid Mac Address Content" );
                    }
                }
                catch ( IOException e )
                {
                    showNotification( e.getMessage() );
                }
            }
        } );
        return SendMagicPackageListButton;
    }


    //Create SendMacgicPackageButton
    private Button createSendMagicPackageButton()
    {
        sendMagicPackageButton = new Button();
        sendMagicPackageButton.setCaption( "Send Specific Magic Package" );
        sendMagicPackageButton.setImmediate( false );
        sendMagicPackageButton.setWidth( "-1px" );
        sendMagicPackageButton.setHeight( "-1px" );

        sendMagicPackageButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                try
                {
                    String macID = macIDTextField.getValue().toString();
                    if ( macID.isEmpty() )
                    {
                        showNotification( "Mac Address Cannot be Empty" );
                    }
                    else
                    {
                        if ( checkMacID( macID ) )
                        {
                            CommandResult commandresult = wolui.getWolManager().sendMagicPackageByMacId( macID );
                            if ( commandresult.hasSucceeded() )
                            {
                                showNotification( "Magic Package Sent to " + macID );
                            }
                            else
                            {
                                showNotification( "Magic Package Cannot be Sent to " + macID );
                            }
                        }
                        else
                        {
                            showNotification( "Invalid Mac Address Content" );
                        }
                    }
                }
                catch ( WolManagerException e )
                {
                    showNotification( e.getMessage() );
                }
            }
        } );
        return sendMagicPackageButton;
    }


    @Override
    public void dispose()
    {
        final WolUI wolUI = null;
    }
}