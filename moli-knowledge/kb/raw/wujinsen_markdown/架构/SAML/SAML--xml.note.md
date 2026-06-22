# 断⾔包含验证断⾔<saml:AuthnStatement>和属性断⾔<saml:AtributeStatement>, SP⽤于访问控制

<saml:Asertion xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:xs="htp:/ w.w3.org/201/XMLSchema" xmlns:xsi="htp:/ w.w3.org/201/XMLSchema-instance" ID="b07b804c-7c29-ea16-730-4f3d6f7928ac" Version="2.0" IsueInstant="204-12-05T09  2 05"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer><!- -> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML 2.0:nameid-format:transient"> 3f7b3dcf-1674-4ecd-92c8-154f346baf8

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML 2.0:cm:bearer"> <saml:SubjectConfirmationData

InResponseTo="af23196-173-213-474a-fe1412ab72" Recipient="htps:/sp.example.com/SAML2/ SO/POST" NotOnOrAfter="204-12-05T09 27 05"/>

</saml:SubjectConfirmation> </saml:Subject> <saml:Conditions

NotBefore="204-12-05T09 17 05" NotOnOrAfter="204-12-05T09 27 05"> <saml:AudienceRestriction>

<saml:Audience>htps:/sp.example.com/SAML2</saml:Audience>

</saml:AudienceRestriction> </saml:Conditions> <saml:AuthnStatement

AuthnInstant="204-12-05T09  2  0" SesionIndex="b07b804c-7c29-ea16-730-4f3d6f7928ac"> <saml:AuthnContext>

<saml:AuthnContextClasRef> urn:oasis:names:tc:SAML 2.0:ac:clases:PaswordProtectedTransport

</saml:AuthnContextClasRef>

</saml:AuthnContext> </saml:AuthnStatement> <saml:AtributeStatement>

<saml:Atribute xmlns:x50="urn:oasis:names:tc:SAML 2.0:profiles:atribute:X50" x50 Encoding="LDAP" NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.1" FriendlyName="eduPersonAfiliation"> <saml:AtributeValue

xsi:type="xs:string">member</saml:AtributeValue> <saml:AtributeValue

xsi:type="xs:string">staf</saml:AtributeValue> </saml:Atribute>

</saml:AtributeStatement> </saml:Asertion>

