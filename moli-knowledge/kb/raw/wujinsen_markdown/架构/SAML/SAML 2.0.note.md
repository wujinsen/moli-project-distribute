From Wikipedia, the fre encyclopedia

Security Asertion Markup Language 2.0 (SAML 2.0) is a version of the standard for exchanging and data betwen . SAML 2.0 is an

SAML

authentication authorization security domains X ML protocol security tokens asertions

-based that uses containing to pas information about a principal (usualy an end user) betwen a SAML authority, that is, an , and a SAML consumer, that is, a . SAML 2.0 enables web-based authentication and authorization scenarios including cros-domain (SO), which helps reduce the administrative overhead of distributing multiple authentication tokens to the user.

identity provider service provider

single sign-on

[1]

SAML 2.0 was ratified as an Standard in March 205, replacing . The critical aspects of SAML 2.0 are covered in detail in the oficial documents SAMLConform,

OASIS SAML 1.1

SAMLCore, SAMLBind, and SAMLProf.

[2] [3] [4] [5]

Some 30 individuals from more than two dozen companies and organizations were involved in the creation of SAML 2.0. In particular, and of special note, donated its Identity Federation Framework (ID-F) specification to OASIS, which became the basis of the SAML 2.0 specification. Thus SAML 2.0 represents the convergence of ,

Liberty Aliance

SAML 1.1Liberty ID-F F 1.2 Shi boleth 1.3

, and .

Contents

[hide]

- 1SAML 2.0 Asertions

- 2SAML 2.0 Protocols

- 2.1Authentication Request Protocol

- 2.2Artifact Resolution Protocol


- 3SAML 2.0 Bindings

- 3.1HTP Redirect Binding

- 3.2HTP POST Binding

- 3.3HTP Artifact Binding 3.3.1Artifact Format


- 4SAML 2.0 Profiles 4.1Web Browser SO Profile

- 4.1.1SP POST Request; IdP POST Response

- 4.1.2SP Redirect Artifact; IdP Redirect Artifact


4.2Identity Provider Discovery Profile 4.3Asertion Query/Request Profile 4.3.1SAML Atribute Query

- 5SAML 2.0 Metadata 5.1Identity Provider Metadata

- 5.1.1SO Service Metadata

5.2Service Provider Metadata

- 5.2.1Asertion Consumer Service Metadata


5.3Metadata Agregates

- 6Se also 7References


# SAML 2.0 Asertions[edit]

<table>
  <tr>
    <th>![image 1](<SAML 2.0.note_images/imageFile1.png>)</th>
    <th>This section only describes one highly specialized aspect of its asociated subject. Please help by ading more general information. The<br><br>improve this article talk</th>
  </tr>
</table>


page may contain sugestions. (October 209)

An asertion is a package of information that suplies zero or more statements made by a SAML authority. SAML asertions are usualy made about a subject, represented by the <Subject> element. The SAML 2.0 specification defines thre diferent kinds of asertion statements that can be created by a SAML authority. Al SAML-defined statements are asociated with a subject. The thre kinds of statements defined are as folows:

Authentication Asertion: The asertion subject was authenticated by a particular means at a particular time. Atribute Asertion: The asertion subject is asociated with the suplied atributes. Authorization Decision Asertion: A request to alow the asertion subject to aces the specified resource has ben granted or denied.

An important type of SAML asertion is the so-caled "bearer" asertion used to facilitate Web Browser SO. Here is an example of a short-lived bearer asertion isued by an identity provider (htps:/idp.example.org/SAML2) to a service provider (htps:/sp.example.com/SAML2). The asertion includes both an Authentication Asertion<saml:AuthnStatement> and an Atribute Asertion<saml:AttributeStatement>, which presumably the service provider uses to make an aces control decision. The prefixsaml: represents the SAML V2.0 asertion namespace.

<saml:Asertion xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:xs="htp:/ w.w3.org/201/XMLSchema" xmlns:xsi="htp:/ w.w3.org/201/XMLSchema-instance" ID="b07b804c-7c29-ea16-730-4f3d6f7928ac" Version="2.0" IsueInstant="204-12-05T09  2 05"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <ds:Signature

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

Note that in the above example the<saml:Assertion> element contains the folowing child elements:

a<saml:Issuer> element, which contains the unique identifier of the identity provider a<ds:Signature> element, which contains an integrity-preserving digital signature (not shown) over the<saml:Assertion> element a<saml:Subject> element, which identifies the authenticated principal (but in this case the identity of the principal is hi den behind an opaque transient identifier, for reasons of privacy) a<saml:Conditions> element, which gives the conditions under which the asertion is to be consideredvalid a<saml:AuthnStatement> element, which describes the act of authentication at the identity provider a<saml:AttributeStatement> element, which aserts a multi-valued atribute asociated with the authenticated principal

In words, the asertion encodes the folowing information:

The asertion ("b07b804c-7c29-ea16-730-4f3d6f7928ac") was isued at time "204-1205T09  2 05Z" by identity provider (htps:/idp.example.org/SAML2) regarding subject (3f7b3dcf-1674-4ecd-92c8-154f346baf8) exclusively for service provider (htps:/sp.example.com/SAML2).

The authentication statement, in particular, aserts the folowing:

The principal identified in the<saml:Subject> element was authenticated at time "204-1205T09  2  0" by means of a pasword sent over a protected chanel.

Likewise the atribute statement aserts that:

The principal identified in the<saml:Subject> element is a staf member at this institution.

# SAML 2.0 Protocols[ ]

edit

The folowing protocols are specified in SAMLCore:

[3]

Asertion Query and Request Protocol Authentication Request Protocol Artifact Resolution Protocol Name Identifier Management Protocol Single Logout Protocol Name Identifier Maping Protocol

The most important of these protocols—the Authentication Request Protocol—is discused in detail below.

Authentication Request Protocol[ ]

edit

In Web Browser SO Profiles are IdP-initiated, that is, an unsolicited<samlp:Response> element is transmited from the identity provider to the service provider (via the browser). (The prefixsamlp: denotes the SAML protocol namespace.) In SAML 2.0, however, the flow begins at the service provider who isues an explicit authentication request to the identity provider. The resultingAuthentication Request Protocol is a significant new feature of SAML 2.0.

- SAML 1.1


When a principal (or an entity acting on the principal's behalf) wishes to obtain an asertion containing an authentication statement, a<samlp:AuthnRequest>element is transmited to the identity provider:

<samlp:AuthnRequest xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="af23196-173-213-474a-fe1412ab72" Version="2.0" IsueInstant="204-12-05T09 21 59" AsertionConsumerServiceIndex="0" AtributeConsumingServiceIndex="0"> <saml:Isuer>htps:/sp.example.com/SAML2</saml:Isuer> <samlp:NameIDPolicy

AlowCreate="true" Format="urn:oasis:names:tc:SAML 2.0:nameid-format:transient"/>

</samlp:AuthnRequest>

The above<samlp:AuthnRequest> element, which implicitly requests

an asertion containing an authentication statement

, was evidently isued by a service provider (htps:/sp.example.com/SAML2) and subsequently presented to the identity provider (via the browser). The identity provider authenticates the principal (if necesary) and isues an authentication response, which is transmited back to the service provider (again via the browser).

Artifact Resolution Protocol[ ]

edit

A SAML mesage is transmited from one entity to another eitherby value orby reference. A reference to a SAML mesage is caled anartifact. The receiver of an artifact resolves the reference by sending a<samlp:ArtifactResolve> request directly to the isuer of the artifact, who then responds with the actual mesage referenced by the artifact.

Supose, for example, that an identity provider sends the folowing<samlp:ArtifactResolve> request directly to a service provider (via a back chanel):

<samlp:ArtifactResolve xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="_ce4e769ed970b501d680f697989d14" Version="2.0" IsueInstant="204-12-05T09 21 58"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <!- an ArtifactResolve mesage SHOULD be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature>

<samlp:Artifact>AQAMh48/1oXIM+sDo7Dh2qMp1HM4IF5DaRNmDj6RdUmlwn9jJHyEgIi8

=</samlp:Artifact> </samlp:ArtifactResolve>

In response, the service provider returns the SAML element referenced by the enclosed artifact. This protocol forms the basis of the .

HTP Artifact Binding

# SAML 2.0 Bindings[ ]

edit

Thebindings suported by SAML 2.0 are outlined in the Bindings specification (SAMLBind ):

[4]

SAML SOAP Binding (based on SOAP 1.1) Reverse SOAP (PAOS) Binding HTP Redirect Binding HTP POST Binding HTP Artifact Binding SAML URI Binding

For Web Browser SO, the HTP Redirect Binding and the HTP POST Binding are comonly used. For example, the service provider may use HTP Redirect to send a request while the identity provider uses HTP POST to transmit the response. This example ilustrates that an entity's choice of binding is independent of its partner's choice of binding.

HTP Redirect Binding[ ]

edit

SAML protocol mesages are often caried directly in the URL query string of an HTP GET request. Since the length of URLs is limited in practice, the HTP Redirect binding is suitable for short mesages, such as the<samlp:AuthnRequest> mesage. Longer mesages (e.g., those containing signed SAML asertions) should be transmited via other bindings such as the

HT P POST Binding

.

SAML requests or responses transmited via HTP Redirect have aSAMLRequest orSAMLResponse query string parameter, respectively. Before itʼs sent, the mesage is deflated (sans header and checksum), base64-encoded, and URL-encoded, in that order. Upon receipt, the proces is reversed to recover the original mesage.

For example, encoding the<samlp:AuthnRequest> mesage above yields:

htps:/idp.example.org/SAML2/ SO/Redirect? SAMLRequest=fZFfa8IwFMXfBb9DyXvaJtZ1BqsURC2

Mabw95ivc5Am3TJrXPf mLY3%2FA15Pzuyf3On8XJXBCaxTRmeEhTEJQBdmr%2FRbRp6 3K3pL5rPhYOpkVdY

ib%2FCon%2BC9AYfDQRB4WDvRv WksVoY6ZQTWlbgBZik9%2FfCR7GorYGTWFK8pu6Dk nwKL%2FWEetlxmR8s

BHbHJDWZqOKGdsRJM0kfQAjCUJ43KX8s78ctnIz%2Blp5xpYa4dSo1fjOKGM03i8jSeCMzGev Ha2%2FBK5MNo1F

dgN2JMqPLmHc0b6WTmiVbsGoTf5qv6Zq2t60x0wXZ2RKydiCJXh3CWV1CWJgqanfl0%2B in8xutxYOvZL18NK

UqPlvZR5el%2BVhYkAgZQdsA6fWVsZXE63W2itrTQ2cVaKV2Cj SqL1v9P%2FAXv4C

The above mesage (formated for readability) may be signed for aditional security. In practice the<samlp:AuthnRequest> mesage is unsigned, leaving the identity provider to identify the sender via .

SAML metadata

HTP POST Binding[ ]

edit

In the folowing example, both the service provider and the identity provider use an HTP POST Binding. Initialy, the service provider responds to a request from the user agent with a document containing an XHTML form:

<formmethod="post"action="htps:/idp.example.org/SAML2/ SO/POST".> <inputtype="hi den"name="SAMLRequest"value="'request'" />

. other input parameter . </form>

The value of theSAMLRequest parameter is the base64-encoding of a<samlp:AuthnRequest> element, which is transmited to the identity provider via the browser. The SO service at the identity provider validates the request and responds with a document containing another XHTML form:

<formmethod="post"action="htps:/sp.example.com/SAML2/ SO/POST".> <inputtype="hi den"name="SAMLResponse"value="'response'" />

. </form>

The value of theSAMLResponse parameter is the base64 encoding of a<samlp:Response> element, which likewise is transmited to the service provider via the browser.

To automate the submision of the form, the folowing line of JavaScript may apear anywhere on the XHTML page:

window.onload=function () {document.forms[0].submit(); }

This asumes, of course, that the first form element in the page contains the above SAMLResponse containingform element (forms[0]).

HTP Artifact Binding[ ]

edit

The HTP Artifact Binding uses the and the SAML SOAP Binding (over HTP) to resolve a SAML mesage by reference. Consider the folowing specific example. Supose a service provider wants to send a<samlp:AuthnRequest> mesage to an identity provider. Initialy, the identity provider transmits an artifact to the service provider via an HTP redirect:

Artifact Resolution Protocol

htps:/sp.example.org/SAML2/ SO/Artifact?SAMLart=artifact

Next the service provider sends a<samlp:ArtifactResolve> request (such as the

ArtifactResolv eRequest

shown earlier) directly to the identity provider via a back chanel. Finaly, the identity provider returns a<samlp:ArtifactResponse> element containing the referenced<samlp:AuthnRequest> mesage:

<samlp:ArtifactResponse xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" ID="_d84a49e595803dedcf4c984c2b0d95" InResponseTo="_ce4e769ed970b501d680f697989d14" Version="2.0" IsueInstant="204-12-05T09 21 59"> <!- an ArtifactResponse mesage SHOULD be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML 2.0:status:Suces"/> </samlp:Status> <samlp:AuthnRequest

xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="_306f8ec5b618f361c70b6fb1480eade" Version="2.0" IsueInstant="204-12-05T09 21 59" Destination="htps:/idp.example.org/SAML2/ SO/Artifact" ProtocolBinding="urn:oasis:names:tc:SAML 2.0:bindings:HTP-Artifact" AsertionConsumerServiceURL="htps:/sp.example.com/SAML2/ SO/Artifact"> <saml:Isuer>htps:/sp.example.com/SAML2</saml:Isuer> <samlp:NameIDPolicy

AlowCreate="false" Format="urn:oasis:names:tc:SAML 1.1:nameid-format:emailAdres"/>

</samlp:AuthnRequest> </samlp:ArtifactResponse>

Of course the flow can go in the other direction as wel, that is, the identity provider may isue an artifact, and in fact this is more comon. Se, for example, the " " profile example later in this topic.

double artifact

Artifact Format[ ]

edit

In general, a SAML 2.0artifact is defined as folows (SAMLBind ):

[4]

SAML_artifact := B64 (TypeCode EndpointIndex RemainingArtifact) TypeCode := Byte1Byte2 EndpointIndex := Byte1Byte2

Thus a SAML 2.0 artifact consists of thre components: a two-byteTypeCode, a twobyteEndpointIndex, and an arbitrary sequence of bytes caled theRemainingArtifact. These thre pieces of information are concatenated and base64-encoded to yield the complete artifact.

TheTypeCode uniquely identifies the artifact format. SAML 2.0 predefines just one such artifact, of type 0x 04. TheEndpointIndex is a reference to a particular artifact resolution endpoint managed by the artifact isuer (which may be either the IdP or the SP, as mentioned earlier). TheRemainingArtifact, which is determined by the type definition, is the "meat" of the artifact.

The format of atype 0x 04 artifact is further defined as folows:

TypeCode := 0x 04 RemainingArtifact := SourceId MesageHandle SourceId := 20-byte_sequence MesageHandle := 20-byte_sequence

Thus a type 0x 04 artifact is of size 4 bytes (unencoded). TheSourceId is an arbitrary sequence of bytes, although in practice, theSourceId is the SHA-1 hash of the isuer's entityID. TheMessageHandle is a random sequence of bytes that references a SAML mesage that the artifact isuer is wiling to produce on-demand.

For example, consider this hex-encoded type 0x 04 artifact:

04 0c878f3fd685c83eb03a3b0e1da329d4738205e43691360e3e917549a59709f d8c91f2120 2f

If you l ok closely, you can se theTypeCode (0x 04) and theEndpointIndex (0x 0) at the front of the artifact. The next 20 bytes are the SHA-1 hash of the isuer's entityID (htps:/idp.example.org/SAML2) folowed by 20 random bytes. The base64-encoding of these

4 bytes is what you se in the example above.

ArtifactResolveRequest

# SAML 2.0 Profiles[ ]

edit

In SAML 2.0, as in SAML 1.1, the primary use case is stil Web Browser SO, but the scope of SAML 2.0 is broader than previous versions of SAML, as sugested in the folowing exhaustive list of profiles:

SO Profiles Web Browser SO Profile Enhanced Client or Proxy (ECP) Profile Identity Provider Discovery Profile Single Logout Profile Name Identifier Management Profile

Artifact Resolution Profile Asertion Query/Request Profile Name Identifier Maping Profile SAML Atribute Profiles

Basic Atribute Profile X.50/LDAP Atribute Profile

UID Atribute Profile DCE PAC Atribute Profile XACML Atribute Profile

Although the number of suported profiles is quite large, the Profiles specification (SAMLProf

) is simplified since the binding aspects of each profile have ben factored out into a separate Bindings specification (SAMLBind ). Web Browser SO Profile[ ]

[5]

[4]

edit

SAML 2.0 specifies aWeb Browser SO Profile involving an identity provider (IdP), a service provider (SP), and a principal wielding an HTP user agent. The SP has four bindings from which to chose while the IdP has thre, which leads to twelve (12) posible deployment scenarios. We outline two such deployment scenarios below.

SP POST Request; IdP POST Response[ ]

edit

This is a relatively simple deployment of the SAML 2.0 Web Browser SO Profile where both the service provider (SP) and the identity provider (IdP) use the HTP POST binding.

![image 2](<SAML 2.0.note_images/imageFile2.png>)

SAML 2.0 Web Browser SO (POST)

The mesage flow begins with a request for a secured resource at the SP.

- 1. Request the target resource at the SP The principal (via an HTP user agent) requests a target resource at the service provider:

htps:/sp.example.com/myresource

The service provider performs a security check on behalf of the target resource. If a valid security context at the service provider already exists, skip steps 2–7.

- 2. Respond with an XHTML form


The service provider responds with a document containing an XHTML form:

<formmethod="post"action="htps:/idp.example.org/SAML2/ SO/POST".> <inputtype="hi den"name="SAMLRequest"value="request" /> <inputtype="hi den"name="RelayState"value="token" />

.

<inputtype="submit"value="Submit" /> </form>

TheRelayState token is an opaque reference to state information maintained at the service

- provider. The value of theSAMLRequest parameter is the base64 encoding of the folowing<samlp:AuthnRequest> element:


<samlp:AuthnRequest xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_1" Version="2.0" IsueInstant="204-12-05T09 21 59" AsertionConsumerServiceIndex="0"> <saml:Isuer>htps:/sp.example.com/SAML2</saml:Isuer> <samlp:NameIDPolicy

AlowCreate="true" Format="urn:oasis:names:tc:SAML 2.0:nameid-format:transient"/>

</samlp:AuthnRequest>

Before the<samlp:AuthnRequest> element is inserted into the XHTML form, it is first base64encoded.

- 3. Request the SO Service at the IdP The user agent isues a POST request to the SO service at the identity provider:


POST/SAML2/ SO/POSTHTP/1.1 Host: idp.example.org Content-Type: aplication/x- w-form-urlencoded Content-Length: n

SAMLRequest=request&RelayState=token

where the values of theSAMLRequest andRelayState parameters are taken from the XHTML form at step 2. The SO service proceses the<samlp:AuthnRequest>element (by URLdecoding, base64-decoding and inflating the request, in that order) and performs a security check. If the user does not have a valid security context, the identity provider identifies the

- user (details omited).


- 4. Respond with an XHTML form


The SO service validates the request and responds with a document containing an XHTML form:

<formmethod="post"action="htps:/sp.example.com/SAML2/ SO/POST".> <inputtype="hi den"name="SAMLResponse"value="response" /> <inputtype="hi den"name="RelayState"value="token" />

.

<inputtype="submit"value="Submit" /> </form>

The value of theRelayState parameter has ben preserved from step 3. The value of theSAMLResponse parameter is the base64 encoding of the folowing<samlp:Response> element:

<samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_2" InResponseTo="identifier_1" Version="2.0" IsueInstant="204-12-05T09  2 05" Destination="htps:/sp.example.com/SAML2/ SO/POST"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <samlp:Status>

<samlp:StatusCode Value="urn:oasis:names:tc:SAML 2.0:status:Suces"/>

</samlp:Status> <saml:Asertion

xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_3" Version="2.0" IsueInstant="204-12-05T09  2 05"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <!- a POSTed asertion MUST be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML 2.0:nameid-format:transient"> 3f7b3dcf-1674-4ecd-92c8-154f346baf8

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML 2.0:cm:bearer"> <saml:SubjectConfirmationData

InResponseTo="identifier_1" Recipient="htps:/sp.example.com/SAML2/ SO/POST" NotOnOrAfter="204-12-05T09 27 05"/>

</saml:SubjectConfirmation> </saml:Subject> <saml:Conditions

NotBefore="204-12-05T09 17 05" NotOnOrAfter="204-12-05T09 27 05"> <saml:AudienceRestriction>

<saml:Audience>htps:/sp.example.com/SAML2</saml:Audience>

</saml:AudienceRestriction> </saml:Conditions> <saml:AuthnStatement

AuthnInstant="204-12-05T09  2  0" SesionIndex="identifier_3"> <saml:AuthnContext>

<saml:AuthnContextClasRef>

urn:oasis:names:tc:SAML 2.0:ac:clases:PaswordProtectedTransport </saml:AuthnContextClasRef>

</saml:AuthnContext> </saml:AuthnStatement> </saml:Asertion>

</samlp:Response>

- 5. Request the Asertion Consumer Service at the SP


The user agent isues a POST request to the asertion consumer service at the service provider:

POST/SAML2/ SO/POSTHTP/1.1 Host: sp.example.com Content-Type: aplication/x- w-form-urlencoded Content-Length: n

SAMLResponse=response&RelayState=token

where the values of theSAMLResponse andRelayState parameters are taken from the XHTML form at step 4.

- 6. Redirect to the target resource The asertion consumer service proceses the response, creates a security context at the service provider and redirects the user agent to the target resource.

- 7. Request the target resource at the SP again

The user agent requests the target resource at the service provider (again):

htps:/sp.example.com/myresource

- 8. Respond with requested resource Since a security context exists, the service provider returns the resource to the user agent. SP Redirect Artifact; IdP Redirect Artifact[ ]


edit

This is a complex deployment of the SAML 2.0 Web Browser SO Profile where both the service provider (SP) and the identity provider (IdP) use the HTP Artifact binding. Both artifacts are delivered to their respective endpoints via HTP GET.

![image 3](<SAML 2.0.note_images/imageFile3.png>)

SAML 2.0 Web Browser SO (Artifact)

The mesage flow begins with a request for a secured resource at the SP:

- 1. Request the target resource at the SP


The principal (via an HTP user agent) requests a target resource at the service provider:

htps:/sp.example.com/myresource

The service provider performs a security check on behalf of the target resource. If a valid security context at the service provider already exists, skip steps 2–1.

- 2. Redirect to the Single Sign-on (SO) Service at the IdP

The service provider redirects the user agent to the single sign-on (SO) service at the identity provider. ARelayState parameter and aSAMLart parameter are apended to the redirect URL.

- 3. Request the SO Service at the IdP The user agent requests the SO service at the identity provider:

htps:/idp.example.org/SAML2/ SO/Artifact?SAMLart=artifact_1&RelayState=token

wheretoken is an opaque reference to state information maintained at the service provider andartifact_1 is a SAML artifact, both isued at step 2.

- 4. Request the Artifact Resolution Service at the SP


The SO service dereferences the artifact by sending a<samlp:ArtifactResolve> element bound to a SAML SOAP mesage to the artifact resolution service at the service provider:

<samlp:ArtifactResolve xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_1" Version="2.0" IsueInstant="204-12-05T09 21 58" Destination="htps:/sp.example.com/SAML2/ArtifactResolution"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <!- an ArtifactResolve mesage SHOULD be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <samlp:Artifact>'artifact_1'</samlp:Artifact>

</samlp:ArtifactResolve>

where the value of the<samlp:Artifact> element is the SAML artifact transmited at step 3.

- 5. Respond with a SAML AuthnRequest


The artifact resolution service at the service provider returns a<samlp:ArtifactResponse> element (containing an<samlp:AuthnRequest> element) bound to a SAML SOAP mesage to the SO service at the identity provider:

<samlp:ArtifactResponse xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" ID="identifier_2" InResponseTo="identifier_1" Version="2.0" IsueInstant="204-12-05T09 21 59"> <!- an ArtifactResponse mesage SHOULD be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML 2.0:status:Suces"/> </samlp:Status> <samlp:AuthnRequest

xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_3" Version="2.0" IsueInstant="204-12-05T09 21 59" Destination="htps:/idp.example.org/SAML2/ SO/Artifact" ProtocolBinding="urn:oasis:names:tc:SAML 2.0:bindings:HTP-Artifact" AsertionConsumerServiceURL="htps:/sp.example.com/SAML2/ SO/Artifact"> <saml:Isuer>htps:/sp.example.com/SAML2</saml:Isuer> <samlp:NameIDPolicy

AlowCreate="false" Format="urn:oasis:names:tc:SAML 1.1:nameid-format:emailAdres"/>

</samlp:AuthnRequest> </samlp:ArtifactResponse>

The SO service proceses the<samlp:AuthnRequest> element and performs a security check. If the user does not have a valid security context, the identity provider identifies the user (details omited).

- 6. Redirect to the Asertion Consumer Service The SO service at the identity provider redirects the user agent to the asertion consumer service at the service provider. The previousRelayState parameter and a newSAMLart parameter are apended to the redirect URL.

- 7. Request the Asertion Consumer Service at the SP

The user agent requests the asertion consumer service at the service provider:

htps:/sp.example.com/SAML2/ SO/Artifact?SAMLart=artifact_2&RelayState=token

wheretoken is the token value from step 3 andartifact_2 is the SAML artifact isued at step 6.

- 8. Request the Artifact Resolution Service at the IdP


The asertion consumer service dereferences the artifact by sending a<samlp:ArtifactResolve> element bound to a SAML SOAP mesage to the artifact resolution service at the identity provider:

<samlp:ArtifactResolve xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_4" Version="2.0" IsueInstant="204-12-05T09  2 04" Destination="htps:/idp.example.org/SAML2/ArtifactResolution"> <saml:Isuer>htps:/sp.example.com/SAML2</saml:Isuer> <!- an ArtifactResolve mesage SHOULD be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <samlp:Artifact>'artifact_2'</samlp:Artifact>

</samlp:ArtifactResolve>

where the value of the<samlp:Artifact> element is the SAML artifact transmited at step 7.

- 9. Respond with a SAML Asertion


The artifact resolution service at the identity provider returns a<samlp:ArtifactResponse> element (containing an<samlp:Response> element) bound to a SAML SOAP mesage to the asertion consumer service at the service provider:

<samlp:ArtifactResponse xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" ID="identifier_5" InResponseTo="identifier_4" Version="2.0" IsueInstant="204-12-05T09  2 05"> <!- an ArtifactResponse mesage SHOULD be signed-> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <samlp:Status>

<samlp:StatusCode

Value="urn:oasis:names:tc:SAML 2.0:status:Suces"/> </samlp:Status> <samlp:Response

xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_6" InResponseTo="identifier_3" Version="2.0" IsueInstant="204-12-05T09  2 05" Destination="htps:/sp.example.com/SAML2/ SO/Artifact"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <ds:Signature

xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#">.</ds:Signature> <samlp:Status>

<samlp:StatusCode Value="urn:oasis:names:tc:SAML 2.0:status:Suces"/>

</samlp:Status> <saml:Asertion

xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" ID="identifier_7" Version="2.0" IsueInstant="204-12-05T09  2 05"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <!- a Subject element is required-> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML 1.1:nameid-format:emailAdres"> user@mail.example.org

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML 2.0:cm:bearer"> <saml:SubjectConfirmationData

InResponseTo="identifier_3" Recipient="htps:/sp.example.com/SAML2/ SO/Artifact" NotOnOrAfter="204-12-05T09 27 05"/>

</saml:SubjectConfirmation> </saml:Subject> <saml:Conditions

NotBefore="204-12-05T09 17 05" NotOnOrAfter="204-12-05T09 27 05"> <saml:AudienceRestriction>

<saml:Audience>htps:/sp.example.com/SAML2</saml:Audience>

</saml:AudienceRestriction> </saml:Conditions> <saml:AuthnStatement

AuthnInstant="204-12-05T09  2  0" SesionIndex="identifier_7"> <saml:AuthnContext>

<saml:AuthnContextClasRef>

urn:oasis:names:tc:SAML 2.0:ac:clases:PaswordProtectedTransport </saml:AuthnContextClasRef>

</saml:AuthnContext> </saml:AuthnStatement> </saml:Asertion>

</samlp:Response> </samlp:ArtifactResponse>

- 10. Redirect to the target resource


The asertion consumer service proceses the response, creates a security context at the service provider and redirects the user agent to the target resource.

1. Request the target resource at the SP again

The user agent requests the target resource at the service provider (again):

htps:/sp.example.com/myresource

12. Respond with the requested resource Since a security context exists, the service provider returns the resource to the user agent. Identity Provider Discovery Profile[ ] The SAML 2.0Identity Provider Discovery Profile introduces the folowing concepts:

edit

Comon Domain Comon Domain Cokie Comon Domain Cokie Writing Service Comon Domain Cokie Reading Service

As a hypothetical example of aComon Domain, let's supose Example UK (example.co.uk) and Example Deutschland (example.de) belong to the virtual organization Example Global Aliance (example.com). In this example, the domainexample.com is the comon domain. Both Example UK and Example Deutschland have a presence in this domain (uk.example.com and de.example.com, resp.).

TheComon Domain Cokie is a secure browser cokie scoped to the comon domain. For each browser user, this cokie stores a history list of recently visited IdPs. The name and value of the cokie are specified in the IdP Discovery Profile (SAMLProf ).

[5]

After a sucesful act of authentication, the IdP requests theComon Domain Cokie Writing Service. This service apends the IdP's unique identifier to the comon domain cokie. The SP, when it receives an unauthenticated request for a protected resource, requests theComon Domain Cokie Reading Service to discover the browser user's most recently used IdP.

Asertion Query/Request Profile[ ]

edit

TheAsertion Query/Request Profile is a general profile that acomodates numerous types of so-caledqueries using the folowing SAML 2.0 elements:

the<samlp:AssertionIDRequest> element, which is used to request an asertion given its unique identifier (ID) the<samlp:SubjectQuery> element, which is an abstract extension point that alows new subject-based SAML queries to be defined the<samlp:AuthnQuery> element, which is used to requestexisting authentication asertions about a given subject from an Authentication Authority the<samlp:AttributeQuery> element, which is used to request atributes about a given subject from an Atribute Authority

the<samlp:AuthzDecisionQuery> element, which is used to request an authorization decision from a trusted third party

The SAML SOAP binding is often used in conjunction with queries. SAML Atribute Query[ ]

edit

TheAtribute Query is perhaps the most important type of SAML query. Often a requester, acting on behalf of the principal, queries an identity provider for atributes. Below we give an example of a query isued by a principal directly:

<samlp:AtributeQuery xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:samlp="urn:oasis:names:tc:SAML 2.0:protocol" ID="af23196-173-213-474a-fe1412ab72" Version="2.0" IsueInstant="206-07-17T20 31 40"> <saml:Isuer

Format="urn:oasis:names:tc:SAML 1.1:nameid-format:X509SubjectName"> CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US

</saml:Isuer> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML 1.1:nameid-format:X509SubjectName"> CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US

</saml:NameID> </saml:Subject> <saml:Atribute

NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:2.5.4.42" FriendlyName="givenName">

</saml:Atribute> <saml:Atribute

NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:1.3.6.1.4.1.146.15.121.1.26" FriendlyName="mail">

</saml:Atribute> </samlp:AtributeQuery>

## Note that theIssuer is theSubject in this case. This is sometimes caled anatribute selfquery. An identity provider might return the folowing asertion, wraped in a<samlp:Response> element (not shown):

<saml:Asertion xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:xs="htp:/ w.w3.org/201/XMLSchema" xmlns:xsi="htp:/ w.w3.org/201/XMLSchema-instance" xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#" ID="_376a319493ad607b7ab3e689482e45" Version="2.0" IsueInstant="206-07-17T20 31 41"> <saml:Isuer>htps:/idp.example.org/SAML2</saml:Isuer> <ds:Signature>.</ds:Signature> <saml:Subject>

<saml:NameID Format="urn:oasis:names:tc:SAML 1.1:nameid-format:X509SubjectName"> CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US

</saml:NameID> <saml:SubjectConfirmation

Method="urn:oasis:names:tc:SAML 2.0:cm:holder-of-key"> <saml:SubjectConfirmationData>

<ds:KeyInfo>

<ds:X509Data> <!- principal's X.509 cert -> <ds:X509Certificate>

MICiDCAXACQDE+9eiWrm62jANBgkqhkiG9w0BAQFADBFMQswCQYDVQGEwJV UzESMBAGA1UEChMJTkNTQS1URVNUMQ0wCwYDVQLEwRVc2VyMRMwEQYDVQDEwpT UC1TZXJ2aWNlMB4XDTA2MDcxNzIwMjE0MVoXDTA2MDcxODIwMjE0MVowSzELMAkG A1UEBhMCVMxEjAQBgNVBAoTCU5DU0EtVEVTVDENMAsGA1UECxMEVXNlcjEZMBcG A1UEAwQdHJzY2F2b0B1aXVjLmVkdTCBnzANBgkqhkiG9w0BAQEFAOBjQAwgYkC gYEAv9QMe4lRl3XbWPcflbCjGK9gty6zBJmp+tsaJINM0VaBaZ3t+tSXknelYife nCc2O3yaX76aq53QMXy+5wKQYe8Rzdw28Nv3a73wfjXJXoUhGkvERcscs9EfIWcC g2bHOg8uSh+Fbv3lHih4lBJ5MCS2buJfsR7dlr/xsadU2RcCAwEATANBgkqhkiG 9w0BAQFAOCAQEAdyIcMTob7TVkelfJ7+I1j0LO24UlKvbLzd2OPvcFTCv6fVHx Ejk0QxaZXJhreZ6+rIdiMXrEzlRdJEsNMxtDW8+sVp6avoB5EX1y3ez+CEAIL4g cjvKZUR4dMryWshWIBHKFul+r7urUgvWI12KbMeE9KP+kiTskLcKgFzngw1J selmHhTcTCrcDocn5yO2+d3dog52vSOtVFDBsBuvDixO2hv679JR6Hlqjtk4GExp E9iVI0wdPE038uQIJTXlhs MLvUGVh/c0ReJBn92Vj4dI/y6PtY/8ncYLYNkjg oVN0J/ymOktn9lTlFyTiuY4OuJsZRO1+zWLy9g=

</ds:X509Certificate> </ds:X509Data>

</ds:KeyInfo> </saml:SubjectConfirmationData>

</saml:SubjectConfirmation> </saml:Subject> <!- asertion lifetime constrained by principal's X.509 cert -> <saml:Conditions

NotBefore="206-07-17T20 31 41" NotOnOrAfter="206-07-18T20 21 41">

</saml:Conditions> <saml:AuthnStatement

AuthnInstant="206-07-17T20 31 41"> <saml:AuthnContext>

<saml:AuthnContextClasRef>

urn:oasis:names:tc:SAML 2.0:ac:clases:TLSClient </saml:AuthnContextClasRef>

</saml:AuthnContext> </saml:AuthnStatement> <saml:AtributeStatement>

<saml:Atribute xmlns:x50="urn:oasis:names:tc:SAML 2.0:profiles:atribute:X50" x50 Encoding="LDAP" NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:2.5.4.42" FriendlyName="givenName"> <saml:AtributeValue

xsi:type="xs:string">Tom</saml:AtributeValue> </saml:Atribute> <saml:Atribute

xmlns:x50="urn:oasis:names:tc:SAML 2.0:profiles:atribute:X50" x50 Encoding="LDAP" NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:1.3.6.1.4.1.146.15.121.1.26" FriendlyName="mail"> <saml:AtributeValue

xsi:type="xs:string">trscavo@gmail.com</saml:AtributeValue>

</saml:Atribute>

</saml:AtributeStatement> </saml:Asertion>

In contrast to the shown earlier, this asertion has a longer lifetime coresponding to the lifetime of the X.509 certificate that the principal used to authenticate to the identity provider. Moreover, since the asertion is signed, the user can push this asertion to a relying party, and as long as the user can prove posesion of the coresponding private key (hence the name "holder-of-key"), the relying party can be asured that the asertion is authentic.

BearerAsertion

SAML 2.0 Metadata[ ]

edit

<table>
  <tr>
    <th>![image 4](<SAML 2.0.note_images/imageFile4.png>)</th>
    <th>This section may require to met Wikipedia's . (April 2012)<br><br>cleanup quality standards</th>
  </tr>
</table>


Quite literaly, metadata is what makes SAML work (or work wel). Let's l ok at some important

- uses of metadata:


An identity provider receives an<samlp:AuthnRequest> element from a service provider via the browser. How does the identity provider know the service provider is authentic and not some evil service provider trying to phish private information regarding the user? The identity provider consults its list of trusted service providersin metadata before isuing an authentication response. In the previous scenario, how does the identity provider know where to redirect the user with the authentication response? The identity provider l oks up a pre-aranged endpoint location of the service providerin metadata. How does the service provider know that the authentication response came from a trusted identity provider? The service provider validates the signature on the asertion using the public key of the identity providerfrom metadata. How does the service provider know where to resolve an artifact from a trusted identity provider? The service provider l oks up the pre-aranged endpoint location of the identity provider's artifact resolution servicefrom metadata.

Metadata ensures a secure transaction betwen an identity provider and a service provider. Before metadata, trust information was encoded into the implementation in a proprietary maner. Now the sharing of trust information is facilitated by standard metadata. SAML 2.0

- provides a wel-defined, interoperable metadata format that entities can leverage to botstrap the trust proces. Identity Provider Metadata[ ]


edit

An identity provider publishes data about itself in an<md:EntityDescriptor> element:

<md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML 2.0:metadata" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#" entityID="htps:/idp.example.org/SAML2"> <!- insert ds:Signature element -> <!- insert md:IDPSODescriptor element (below)-> <!- insert md:AtributeAuthorityDescriptor element (not shown)-> <md:Organization>

<md:OrganizationNamexml:lang="en">

SAML Identity Provider </md:OrganizationName> <md:OrganizationDisplayNamexml:lang="en">

SAML Identity Provider @ Some Location </md:OrganizationDisplayName> <md:OrganizationURLxml:lang="en">

htp:/ w.idp.example.org/

</md:OrganizationURL> </md:Organization> <md:ContactPersoncontactType="technical">

<md:SurName>SAML IdP Suport</md:SurName> <md:EmailAdres>mailto:saml-suport@idp.example.org</md:EmailAdres>

</md:ContactPerson> </md:EntityDescriptor>

TheentityID atribute is the unique identifier of the identity provider. Note that the details of the digital signature (in the<ds:Signature> element) have ben omited from this example.

The identity provider manages an SO service and an atribute authority, each having its own descriptor. We describe SO service metadata below while the<md:AttributeAuthorityDescriptor> element is not shown.

SO Service Metadata[ ]

edit

The SO service at the identity provider is described in an<md:IDPSSODescriptor> element:

<md:IDPSODescriptor protocolSuportEnumeration="urn:oasis:names:tc:SAML 2.0:protocol"> <md:KeyDescriptoruse="signing">

<ds:KeyInfo> <ds:KeyName>IdP SO Key</ds:KeyName>

</ds:KeyInfo> </md:KeyDescriptor> <md:ArtifactResolutionServiceisDefault="true"index="0"

Binding="urn:oasis:names:tc:SAML 2.0:bindings:SOAP" Location="htps:/idp.example.org/SAML2/ArtifactResolution"/>

<md:NameIDFormat>

- urn:oasis:names:tc:SAML 1.1:nameid-format:emailAdres

</md:NameIDFormat> <md:NameIDFormat>

- urn:oasis:names:tc:SAML 2.0:nameid-format:transient


</md:NameIDFormat> <md:SingleSignOnService

Binding="urn:oasis:names:tc:SAML 2.0:bindings:HTP-POST" Location="htps:/idp.example.org/SAML2/ SO/POST"/>

<md:SingleSignOnService Binding="urn:oasis:names:tc:SAML 2.0:bindings:HTP-Artifact" Location="htps:/idp.example.org/SAML2/Artifact"/>

<saml:Atribute NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.1" FriendlyName="eduPersonAfiliation"> <saml:AtributeValue>member</saml:AtributeValue> <saml:AtributeValue>student</saml:AtributeValue> <saml:AtributeValue>faculty</saml:AtributeValue> <saml:AtributeValue>employe</saml:AtributeValue> <saml:AtributeValue>staf</saml:AtributeValue>

</saml:Atribute> </md:IDPSODescriptor>

The previous metadata element describes the SO service at the identity provider. Note the folowing details about this element:

Key information has ben omited for brevity. TheBinding atribute of the<md:ArtifactResolutionService> element indicates that the SAML SOAP binding (SAMLBind[4]) should be used for artifact resolution. TheLocation atribute of the<md:ArtifactResolutionService> element is used in step 8 of the "double artifact" profile. The value of theindex atribute of the<md:ArtifactResolutionService> element is used as theEndpointIndex in the construction of a SAML type 0x 04 artifact. The<md:NameIDFormat> elements indicate what SAML name identifier formats (SAMLCore ) the SO service suports.

[3]

TheBinding atributes of the<md:SingleSignOnService> elements are standard URIs specified in the SAML 2.0 Binding specification (SAMLBind[4]). TheLocation atribute of the<md:SingleSignOnService> element that suports the HTP POST binding is used in step 2 of the "double POST" profile. TheLocation atribute of the<md:SingleSignOnService> element that suports the HTP Artifact binding is used in step 2 of the "double artifact" profile. The<saml:Attribute> element describes an atribute that the identity provider is wiling to asert (subject to policy). The<saml:AttributeValue> elements enumerate the posible values the atribute may take on.

Service Provider Metadata[ ]

edit

A service provider also publishes data about itself in an<md:EntityDescriptor> element:

<md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML 2.0:metadata" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#" entityID="htps:/sp.example.com/SAML2"> <!- insert ds:Signature element -> <!- insert md:SPSODescriptor element (se below)-> <md:Organization>

<md:OrganizationNamexml:lang="en">

SAML Service Provider </md:OrganizationName> <md:OrganizationDisplayNamexml:lang="en">

SAML Service Provider @ Some Location </md:OrganizationDisplayName> <md:OrganizationURLxml:lang="en">

htp:/ w.sp.example.com/

</md:OrganizationURL> </md:Organization> <md:ContactPersoncontactType="technical">

<md:SurName>SAML SP Suport</md:SurName> <md:EmailAdres>mailto:saml-suport@sp.example.com</md:EmailAdres>

</md:ContactPerson> </md:EntityDescriptor>

The primary component managed by the service provider is the asertion consumer service, which is discused below.

Asertion Consumer Service Metadata[ ]

edit

The asertion consumer service is contained in an<md:SPSSODescriptor> element:

<md:SPSODescriptor protocolSuportEnumeration="urn:oasis:names:tc:SAML 2.0:protocol"> <md:KeyDescriptoruse="signing">

<ds:KeyInfo> <ds:KeyName>SP SO Key</ds:KeyName>

</ds:KeyInfo> </md:KeyDescriptor> <md:ArtifactResolutionServiceisDefault="true"index="0"

Binding="urn:oasis:names:tc:SAML 2.0:bindings:SOAP" Location="htps:/sp.example.com/SAML2/ArtifactResolution"/>

<md:NameIDFormat>

- urn:oasis:names:tc:SAML 1.1:nameid-format:emailAdres </md:NameIDFormat> <md:NameIDFormat>

- urn:oasis:names:tc:SAML 2.0:nameid-format:transient </md:NameIDFormat> <md:AsertionConsumerServiceisDefault="true"index="0"


Binding="urn:oasis:names:tc:SAML 2.0:bindings:HTP-POST" Location="htps:/sp.example.com/SAML2/ SO/POST"/>

<md:AsertionConsumerServiceindex="1" Binding="urn:oasis:names:tc:SAML 2.0:bindings:HTP-Artifact" Location="htps:/sp.example.com/SAML2/Artifact"/>

<md:AtributeConsumingServiceisDefault="true"index="1"> <md:ServiceNamexml:lang="en">

Service Provider Portal </md:ServiceName> <md:RequestedAtribute

NameFormat="urn:oasis:names:tc:SAML 2.0:atrname-format:uri" Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.1" FriendlyName="eduPersonAfiliation">

</md:RequestedAtribute>

</md:AtributeConsumingService> </md:SPSODescriptor>

Note the folowing details about the<md:SPSSODescriptor> metadata element:

Theindex atribute of an<md:AssertionConsumerService> element is used as the value of theAssertionConsumerServiceIndex atribute in a<samlp:AuthnRequest> element. TheBinding atributes of the<md:AssertionConsumerService> elements are standard URIs specified in the SAML 2.0 Binding specification (SAMLBind[4]). TheLocation atribute of the<md:AssertionConsumerService> element that suports the HTP POST binding (index="0") is used in step 4 of the "double POST" profile. TheLocation atribute of the<md:AssertionConsumerService> element that suports the HTP Artifact binding (index="1") is used in step 6 of the "double artifact" profile. The<md:AttributeConsumingService> element is used by the identity provider to formulate an<saml:AttributeStatement> element that is pushed to the service provider in conjunction with Web Browser SO. Theindex atribute of the<md:AttributeConsumingService> element is used as the value of theAttributeConsumingServiceIndex atribute in a<samlp:AuthnRequest> element.

As noted earlier, the values of theLocation atributes are used by an identity provider to route SAML mesages, which minimizes the posibility of a rogue service provider orchestrating a

m an-in-the-mi dle atack

. Metadata Agregates[ ]

edit

In the previous examples, each<md:EntityDescriptor> element is shown to be digitaly signed. In practice, however, multiple<md:EntityDescriptor> elements are grouped together under an<md:EntitiesDescriptor> element with a single digital signature over the entire agregate:

<md:EntitiesDescriptor xmlns:md="urn:oasis:names:tc:SAML 2.0:metadata" xmlns:saml="urn:oasis:names:tc:SAML 2.0:asertion" xmlns:ds="htp:/ w.w3.org/2 0/09/xmldsig#" validUntil="2013-03-2T23  0  0Z"> <!- insert ds:Signature element -> <md:EntityDescriptor

entityID="htps:/idp.example.org/SAML2">

. </md:EntityDescriptor> <md:EntityDescriptor

entityID="htps:/sp.example.com/SAML2">

.

</md:EntityDescriptor> </md:EntitiesDescriptor>

Typicaly metadata agregates such as this are published by trusted third parties caledfederations who vouch for the integrity of al the metadata in the agregate. These metadata agregates can be very large, sometimes on the order of hundreds of entities per agregate.

