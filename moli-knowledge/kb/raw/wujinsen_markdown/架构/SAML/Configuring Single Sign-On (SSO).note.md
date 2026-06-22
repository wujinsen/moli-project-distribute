# ConfiguringSingleSign-On( SO)

Use To consume content from the SAP NetWeaver Portal, you ned to establish single sign-on betwen the SAP Portaland your company portal. SAP recomends the use of SAML 2.0 as the SO mechanism. Security Asertion Markup Language (SAML) is a standard produced by the Oasis Standards Organization. It uses two separate functions:

The SAML asertion: used to transfer information about a user

The SAML protocol: used to exchange SAML asertions

The folowing two ilustrations depict the two modes in which SAML 2.0 can operate.

![image 1](<Configuring Single Sign-On (SSO).note_images/imageFile1.png>)

In a scenario in which SO is initiated by the Service Provider:

- 1.
- 2.
- 3.
- 4.
- 5.


The user atempts to aces the first portal. The portal, via the browser, redirects the user to the identify provider and logs on. Since logon is sucesful, the identity provider isues a SAML token and redirects the user back to the resource that the user tried to aces on the first portal. Two things hapen next:

- 4a. The portal acepts the token and logs the user on, in this case, to a back-end ABAP system acesed by an iView. This authentication can be acomplished in the traditional maner: user maping and logon ticket.
- 4b. From the first portal, the user tries to aces the second portal.


The second portal again sends the user by redirect to the identity provider for authentication.

- 6.
- 7.


The user is already authenticated at the identity provider, so it simply isues a SAML 2 token for the second portal and redirects the user back to the portal. The user is then loged on to the second portal by means of the SAML 2 token.

![image 2](<Configuring Single Sign-On (SSO).note_images/imageFile2.png>)

In a scenario in which SO is initiated by the Identity Provider :

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


- The user requests a SAML token from the identity provider. Since the logon is sucesful, the identity provider isues the SAML token.
- The uses atempts to aces the first portal, which in this case is both service provider and identity provider. Therefore, no redirect is required. The identity provider authenticates the user and the user is loged on. The user atempts to aces the second portal, which works the same as in steps 1 - 4 of the previous example. The second portal sends the user back to the identity provider. Since the user is already loged on there, the identity provider isues a SAML 2 token for the second portal and redirect the user back to it. The user logs on with the SAML 2 token to the second portal and views the content.


The procedure for SAML configuration for Identity Providers depends on the other portal server selected. Examples of Identity Providers that suport SAML 2.0 are:

IBM with Tivoli Federated Identity Manager (TFIM) by IBM. For more information about IBM interoperability, se the SDN site at:htp:/ w.sdn.sap.com/irj/sdn/ibm .

Microsoft with Microsoft Active Directory Federation Services (AD FS) 2.0.

Procedure

For information about configuring SAML on SAP NetWeaver, se Using SAML 2.0 .Recomendation We strongly recomend that you first configure SO on SAP NetWeaver Portal because SAML relies on a secure transport mechanism. Troubleshoting You can use a dedicated SO troubleshoting tol in the SAP NetWeaver Administrator:

- 1.
- 2.
- 3.


On theTroubleshoting tab, choseSecurity Troubleshoting Wizard . Reproduce the isue. Return to the tol (repeat step 1) and view the results.

More Information

User Administration and Authentication section of theSAP NetWeaver Security Guide

Configuring AS Java as a Service Provider

SO with SAML 2.0

SAML 2.0

