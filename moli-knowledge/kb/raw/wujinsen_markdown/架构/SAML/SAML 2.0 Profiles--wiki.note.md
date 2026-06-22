## http://en.wikipedia.org/wiki/SAML_2.0#Web_Browser_SSO_Profile

In SAML 2.0, as in SAML 1.1, the primary use case is still Web Browser SSO, but the scope of SAML 2.0 is broader than previous versions of SAML, as suggested in the following exhaustive list of profiles:

SSO Profiles Web Browser SSO Profile Enhanced Client or Proxy (ECP) Profile Identity Provider Discovery Profile Single Logout Profile Name Identifier Management Profile

Artifact Resolution Profile Assertion Query/Request Profile Name Identifier Mapping Profile SAML Attribute Profiles

Basic Attribute Profile X.500/LDAP Attribute Profile UUID Attribute Profile DCE PAC Attribute Profile XACML Attribute Profile

Although the number of supported profiles is quite large, the Profiles specification (SAMLProf ) is simplified since the binding aspects of each profile have been factored out into a separate Bindings specification (SAMLBind ).

[5]

[4]

Web Browser SSO Profile[ ]

# edit

SAML 2.0 specifies a Web Browser SSO Profile involving an identity provider (IdP), a service provider (SP), and a principal wielding an HTTP user agent. The SP has four bindings from which to choose while the IdP has three, which leads to twelve (12) possible deployment scenarios. We outline two such deployment scenarios below.

# SP POST Request; IdP POST Response[ ]

# edit

This is a relatively simple deployment of the SAML 2.0 Web Browser SSO Profile where both the service provider (SP) and the identity provider (IdP) use the HTTP POST binding.

![image 1](<SAML 2.0 Profiles--wiki.note_images/imageFile1.png>)

SAML 2.0 Web Browser SSO (POST)

The message flow begins with a request for a secured resource at the SP.

## 1. Request the target resource at the SP

The principal (via an HTTP user agent) requests a target resource at the service provider:

htps:/sp.example.com/myresource

The service provider performs a security check on behalf of the target resource. If a valid security context at the service provider already exists, skip steps 2–7.

## 2. Respond with an XHTML form

The service provider responds with a document containing an XHTML form:

<form method="post" action="https://idp.example.org/SAML2/SSO/POST" ...> <input type="hidden" name="SAMLRequest" value="''request''" /> <input type="hidden" name="RelayState" value="''token''" />

... <input type="submit" value="Submit" />

</form>

The RelayState token is an opaque reference to state information maintained at the service provider. The value of the SAMLRequest parameter is the base64 encoding of the following <samlp:AuthnRequest> element:

<samlp:AuthnRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_1" Version="2.0" IssueInstant="2004-12-05T09:21:59" AssertionConsumerServiceIndex="0"> <saml:Issuer>https://sp.example.com/SAML2</saml:Issuer> <samlp:NameIDPolicy AllowCreate="true" Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient"/>

</samlp:AuthnRequest>

Before the <samlp:AuthnRequest> element is inserted into the XHTML form, it is first base64-encoded.

## 3. Request the SSO Service at the IdP

The user agent issues a POST request to the SSO service at the identity provider:

POST /SAML2/ SO/POST HTP/1.1 Host: idp.example.org Content-Type: aplication/x- w-form-urlencoded Content-Length: n

SAMLRequest=request&RelayState=token

where the values of the SAMLRequest and RelayState parameters are taken from the XHTML form at step 2. The SSO service processes the <samlp:AuthnRequest>element (by URL-decoding, base64decoding and inflating the request, in that order) and performs a security check. If the user does not have a valid security context, the identity provider identifies the user (details omitted).

## 4. Respond with an XHTML form

The SSO service validates the request and responds with a document containing an XHTML form:

<form method="post" action="https://sp.example.com/SAML2/SSO/POST" ...> <input type="hidden" name="SAMLResponse" value="''response''" /> <input type="hidden" name="RelayState" value="''token''" />

... <input type="submit" value="Submit" />

</form>

The value of the RelayState parameter has been preserved from step 3. The value of the SAMLResponse parameter is the base64 encoding of the following<samlp:Response> element:

<samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_2" InResponseTo="identifier_1" Version="2.0" IssueInstant="2004-12-05T09:22:05" Destination="https://sp.example.com/SAML2/SSO/POST"> <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML:2.0:status:Success"/> </samlp:Status> <saml:Assertion

xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_3" Version="2.0" IssueInstant="2004-12-05T09:22:05"> <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer> <!-- a POSTed assertion MUST be signed --> <ds:Signature

xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient"> 3f7b3dcf-1674-4ecd-92c8-1544f346baf8

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"> <saml:SubjectConfirmationData

InResponseTo="identifier_1" Recipient="https://sp.example.com/SAML2/SSO/POST" NotOnOrAfter="2004-12-05T09:27:05"/>

</saml:SubjectConfirmation>

</saml:Subject> <saml:Conditions

NotBefore="2004-12-05T09:17:05" NotOnOrAfter="2004-12-05T09:27:05"> <saml:AudienceRestriction>

<saml:Audience>https://sp.example.com/SAML2</saml:Audience>

</saml:AudienceRestriction> </saml:Conditions> <saml:AuthnStatement

AuthnInstant="2004-12-05T09:22:00" SessionIndex="identifier_3">

<saml:AuthnContext> <saml:AuthnContextClassRef>

urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport </saml:AuthnContextClassRef>

</saml:AuthnContext> </saml:AuthnStatement> </saml:Assertion>

</samlp:Response>

## 5. Request the Assertion Consumer Service at the SP

The user agent issues a POST request to the assertion consumer service at the service provider:

POST /SAML2/ SO/POST HTP/1.1 Host: sp.example.com Content-Type: aplication/x- w-form-urlencoded Content-Length: n

SAMLResponse=response&RelayState=token

where the values of the SAMLResponse and RelayState parameters are taken from the XHTML form at step 4.

## 6. Redirect to the target resource

The assertion consumer service processes the response, creates a security context at the service provider and redirects the user agent to the target resource.

## 7. Request the target resource at the SP again

The user agent requests the target resource at the service provider (again):

htps:/sp.example.com/myresource

## 8. Respond with requested resource

Since a security context exists, the service provider returns the resource to the user agent.

# SP Redirect Artifact; IdP Redirect Artifact[ ]

# edit

This is a complex deployment of the SAML 2.0 Web Browser SSO Profile where both the service provider (SP) and the identity provider (IdP) use the HTTP Artifact binding. Both artifacts are delivered to their respective endpoints via HTTP GET.

![image 2](<SAML 2.0 Profiles--wiki.note_images/imageFile2.png>)

SAML 2.0 Web Browser SSO (Artifact)

The message flow begins with a request for a secured resource at the SP:

## 1. Request the target resource at the SP

The principal (via an HTTP user agent) requests a target resource at the service provider:

htps:/sp.example.com/myresource

The service provider performs a security check on behalf of the target resource. If a valid security context at the service provider already exists, skip steps 2–11.

## 2. Redirect to the Single Sign-on (SSO) Service at the IdP

The service provider redirects the user agent to the single sign-on (SSO) service at the identity provider. A RelayState parameter and a SAMLart parameter are appended to the redirect URL.7

## 3. Request the SSO Service at the IdP

The user agent requests the SSO service at the identity provider:

htps:/idp.example.org/SAML2/ SO/Artifact?SAMLart=artifact_1&RelayState=token

where token is an opaque reference to state information maintained at the service provider and artifact_1 is a SAML artifact, both issued at step 2.

## 4. Request the Artifact Resolution Service at the SP

The SSO service dereferences the artifact by sending a <samlp:ArtifactResolve> element bound to a SAML SOAP message to the artifact resolution service at the service provider:

<samlp:ArtifactResolve xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_1" Version="2.0" IssueInstant="2004-12-05T09:21:58" Destination="https://sp.example.com/SAML2/ArtifactResolution"> <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer> <!-- an ArtifactResolve message SHOULD be signed --> <ds:Signature

xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature> <samlp:Artifact>''artifact_1''</samlp:Artifact>

</samlp:ArtifactResolve>

where the value of the <samlp:Artifact> element is the SAML artifact transmitted at step 3.

## 5. Respond with a SAML AuthnRequest

The artifact resolution service at the service provider returns a <samlp:ArtifactResponse> element (containing an <samlp:AuthnRequest> element) bound to a SAML SOAP message to the SSO service at the identity provider:

ID="identifier_2" InResponseTo="identifier_1" Version="2.0" IssueInstant="2004-12-05T09:21:59"> <!-- an ArtifactResponse message SHOULD be signed --> <ds:Signature

xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML:2.0:status:Success"/> </samlp:Status> <samlp:AuthnRequest

xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_3" Version="2.0" IssueInstant="2004-12-05T09:21:59" Destination="https://idp.example.org/SAML2/SSO/Artifact" ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact" AssertionConsumerServiceURL="https://sp.example.com/SAML2/SSO/Artifact"> <saml:Issuer>https://sp.example.com/SAML2</saml:Issuer> <samlp:NameIDPolicy AllowCreate="false" Format="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"/>

</samlp:AuthnRequest> </samlp:ArtifactResponse>

The SSO service processes the <samlp:AuthnRequest> element and performs a security check. If the user does not have a valid security context, the identity provider identifies the user (details omitted).

## 6. Redirect to the Assertion Consumer Service

The SSO service at the identity provider redirects the user agent to the assertion consumer service at the service provider. The previous RelayState parameter and a new SAMLart parameter are appended to the redirect URL.

## 7. Request the Assertion Consumer Service at the SP

The user agent requests the assertion consumer service at the service provider:

htps:/sp.example.com/SAML2/ SO/Artifact?SAMLart=artifact_2&RelayState=token

where token is the token value from step 3 and artifact_2 is the SAML artifact issued at step 6.

## 8. Request the Artifact Resolution Service at the IdP

The assertion consumer service dereferences the artifact by sending a <samlp:ArtifactResolve> element bound to a SAML SOAP message to the artifact resolution service at the identity provider:

<samlp:ArtifactResolve xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_4" Version="2.0" IssueInstant="2004-12-05T09:22:04" Destination="https://idp.example.org/SAML2/ArtifactResolution"> <saml:Issuer>https://sp.example.com/SAML2</saml:Issuer> <!-- an ArtifactResolve message SHOULD be signed --> <ds:Signature

xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature> <samlp:Artifact>''artifact_2''</samlp:Artifact>

</samlp:ArtifactResolve>

where the value of the <samlp:Artifact> element is the SAML artifact transmitted at step 7.

## 9. Respond with a SAML Assertion

The artifact resolution service at the identity provider returns a <samlp:ArtifactResponse> element (containing an <samlp:Response> element) bound to a SAML SOAP message to the assertion consumer service at the service provider:

ID="identifier_5" InResponseTo="identifier_4" Version="2.0" IssueInstant="2004-12-05T09:22:05"> <!-- an ArtifactResponse message SHOULD be signed --> <ds:Signature

xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML:2.0:status:Success"/> </samlp:Status> <samlp:Response

xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_6" InResponseTo="identifier_3" Version="2.0" IssueInstant="2004-12-05T09:22:05" Destination="https://sp.example.com/SAML2/SSO/Artifact"> <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer> <ds:Signature

xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML:2.0:status:Success"/> </samlp:Status> <saml:Assertion

xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="identifier_7" Version="2.0" IssueInstant="2004-12-05T09:22:05"> <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer> <!-- a Subject element is required --> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"> user@mail.example.org

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML:2.0:cm:bearer"> <saml:SubjectConfirmationData

InResponseTo="identifier_3" Recipient="https://sp.example.com/SAML2/SSO/Artifact"

NotOnOrAfter="2004-12-05T09:27:05"/> </saml:SubjectConfirmation>

</saml:Subject> <saml:Conditions

NotBefore="2004-12-05T09:17:05" NotOnOrAfter="2004-12-05T09:27:05"> <saml:AudienceRestriction>

<saml:Audience>https://sp.example.com/SAML2</saml:Audience>

</saml:AudienceRestriction> </saml:Conditions> <saml:AuthnStatement

AuthnInstant="2004-12-05T09:22:00" SessionIndex="identifier_7"> <saml:AuthnContext>

<saml:AuthnContextClassRef>

urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport </saml:AuthnContextClassRef>

</saml:AuthnContext> </saml:AuthnStatement> </saml:Assertion>

</samlp:Response> </samlp:ArtifactResponse>

## 10. Redirect to the target resource

The assertion consumer service processes the response, creates a security context at the service provider and redirects the user agent to the target resource.

## 11. Request the target resource at the SP again

The user agent requests the target resource at the service provider (again):

htps:/sp.example.com/myresource

## 12. Respond with the requested resource

Since a security context exists, the service provider returns the resource to the user agent.

Identity Provider Discovery Profile[ ]

# edit

The SAML 2.0 Identity Provider Discovery Profile introduces the following concepts:

Common Domain Common Domain Cookie Common Domain Cookie Writing Service Common Domain Cookie Reading Service

As a hypothetical example of a Common Domain, let's suppose Example UK (example.co.uk) and Example Deutschland (example.de) belong to the virtual organization Example Global Alliance (example.com). In this example, the domain example.com is the common domain. Both Example UK and Example Deutschland have a presence in this domain (uk.example.com and de.example.com, resp.).

The Common Domain Cookie is a secure browser cookie scoped to the common domain. For each browser user, this cookie stores a history list of recently visited IdPs. The name and value of the cookie are specified in the IdP Discovery Profile (SAMLProf ).

[5]

After a successful act of authentication, the IdP requests the Common Domain Cookie Writing Service. This service appends the IdP's unique identifier to the common domain cookie. An SP, when it receives an unauthenticated request for a protected resource, requests the Common Domain Cookie Reading

Service to discover the browser user's most recently used IdP.

Assertion Query/Request Profile[ ]

# edit

The Assertion Query/Request Profile is a general profile that accommodates numerous types of socalled queries using the following SAML 2.0 elements:

the <samlp:AssertionIDRequest> element, which is used to request an assertion given its unique identifier (ID) the <samlp:SubjectQuery> element, which is an abstract extension point that allows new subject-based SAML queries to be defined the <samlp:AuthnQuery> element, which is used to request existing authentication assertions about a given subject from an Authentication Authority the <samlp:AttributeQuery> element, which is used to request attributes about a given subject from an Attribute Authority the <samlp:AuthzDecisionQuery> element, which is used to request an authorization decision from a trusted third party

The SAML SOAP binding is often used in conjunction with queries.

# SAML Attribute Query[ ]

# edit

The Attribute Query is perhaps the most important type of SAML query. Often a requester, acting on behalf of the principal, queries an identity provider for attributes. Below we give an example of a query issued by a principal directly:

<samlp:AttributeQuery xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" ID="aaf23196-1773-2113-474a-fe114412ab72" Version="2.0" IssueInstant="2006-07-17T20:31:40"> <saml:Issuer

Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName"> CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US

</saml:Issuer> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName"> CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US

</saml:NameID> </saml:Subject> <saml:Attribute

NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri" Name="urn:oid:2.5.4.42" FriendlyName="givenName">

</saml:Attribute> <saml:Attribute

NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri" Name="urn:oid:1.3.6.1.4.1.1466.115.121.1.26" FriendlyName="mail">

</saml:Attribute> </samlp:AttributeQuery>

Note that the Issuer is the Subject in this case. This is sometimes called an attribute self-query. An identity provider might return the following assertion, wrapped in a <samlp:Response> element (not shown):

<saml:Assertion xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ds="http://www.w3.org/2000/09/xmldsig#" ID="_33776a319493ad607b7ab3e689482e45" Version="2.0" IssueInstant="2006-07-17T20:31:41"> <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer> <ds:Signature>...</ds:Signature> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName"> CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML:2.0:cm:holder-of-key"> <saml:SubjectConfirmationData>

<ds:KeyInfo>

<ds:X509Data> <!-- principal's X.509 cert --> <ds:X509Certificate>

MIICiDCCAXACCQDE+9eiWrm62jANBgkqhkiG9w0BAQQFADBFMQswCQYDVQQGEwJV UzESMBAGA1UEChMJTkNTQS1URVNUMQ0wCwYDVQQLEwRVc2VyMRMwEQYDVQQDEwpT UC1TZXJ2aWNlMB4XDTA2MDcxNzIwMjE0MVoXDTA2MDcxODIwMjE0MVowSzELMAkG A1UEBhMCVVMxEjAQBgNVBAoTCU5DU0EtVEVTVDENMAsGA1UECxMEVXNlcjEZMBcG A1UEAwwQdHJzY2F2b0B1aXVjLmVkdTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkC gYEAv9QMe4lRl3XbWPcflbCjGK9gty6zBJmp+tsaJINM0VaBaZ3t+tSXknelYife nCc2O3yaX76aq53QMXy+5wKQYe8Rzdw28Nv3a73wfjXJXoUhGkvERcscs9EfIWcC g2bHOg8uSh+Fbv3lHih4lBJ5MCS2buJfsR7dlr/xsadU2RcCAwEAATANBgkqhkiG 9w0BAQQFAAOCAQEAdyIcMTob7TVkelfJ7+I1j0LO24UlKvbLzd2OPvcFTCv6fVHx Ejk0QxaZXJhreZ6+rIdiMXrEzlRdJEsNMxtDW8++sVp6avoB5EX1y3ez+CEAIL4g cjvKZUR4dMryWshWIBHKFFul+r7urUgvWI12KbMeE9KP+kiiiiTskLcKgFzngw1J selmHhTcTCrcDocn5yO2+d3dog52vSOtVFDBsBuvDixO2hv679JR6Hlqjtk4GExp E9iVI0wdPE038uQIJJTXlhsMMLvUGVh/c0ReJBn92Vj4dI/yy6PtY/8ncYLYNkjg oVN0J/ymOktn9lTlFyTiuY4OuJsZRO1+zWLy9g==

</ds:X509Certificate> </ds:X509Data>

</ds:KeyInfo> </saml:SubjectConfirmationData>

</saml:SubjectConfirmation> </saml:Subject> <!-- assertion lifetime constrained by principal's X.509 cert --> <saml:Conditions

NotBefore="2006-07-17T20:31:41"

NotOnOrAfter="2006-07-18T20:21:41"> </saml:Conditions> <saml:AuthnStatement

AuthnInstant="2006-07-17T20:31:41"> <saml:AuthnContext>

<saml:AuthnContextClassRef>

urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient </saml:AuthnContextClassRef>

</saml:AuthnContext> </saml:AuthnStatement> <saml:AttributeStatement>

<saml:Attribute xmlns:x500="urn:oasis:names:tc:SAML:2.0:profiles:attribute:X500" x500:Encoding="LDAP" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri" Name="urn:oid:2.5.4.42" FriendlyName="givenName"> <saml:AttributeValue

xsi:type="xs:string">Tom</saml:AttributeValue> </saml:Attribute> <saml:Attribute

xmlns:x500="urn:oasis:names:tc:SAML:2.0:profiles:attribute:X500" x500:Encoding="LDAP" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri" Name="urn:oid:1.3.6.1.4.1.1466.115.121.1.26" FriendlyName="mail"> <saml:AttributeValue

xsi:type="xs:string">trscavo@gmail.com</saml:AttributeValue> </saml:Attribute>

</saml:AttributeStatement> </saml:Assertion>

In contrast to the shown earlier, this assertion has a longer lifetime corresponding to the lifetime of the X.509 certificate that the principal used to authenticate to the identity provider. Moreover, since the assertion is signed, the user can push this assertion to a relying party, and as long as the user can prove possession of the corresponding private key (hence the name "holder-of-key"), the relying party can be assured that the assertion is authentic.

BearerAssertion

