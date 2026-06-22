# Prerequisites

You have been assigned a role with the required authorizations.

For more information, see Application Server Java as an SAML 2.0 Provider .

You have created any necessary keys and certificates in a keystore view dedicated to SAML.

For more information, see Additional Keystore and Cryptographic Functions .

There is a SAML 2.0 identity provider in your SAML network.

The identity provider can be in the same local area network or in another domain.

For more information about the identity provider available with SAP Single Sign-On 2.0, see

Identity Provider Implemen tation Guide

.

# Context

This procedure provides an overview of the steps to configure SAP NetWeaver Application Server (AS) Java as a Security Assertion Markup Language (SAML) 2.0 service provider. As a service provider, the AS Java enables you to off-load the authentication of users onto an identity provider. The identity provider enables you to federate identities across domains for Single Sign-On (SSO). Once logged on, SAML 2.0 enables Single Log-Out (SLO).

# Procedure

- 1.
- 2.
- 3.
- 4.
- 5.


Enable SAML 2.0 support and select the certificates for digital signatures and encryption.

For more information, see .

Enabling the SAML Service Provider

Determine how your service provider communicates with identity providers.

For more information, see the following:

Configuring Front-Channel Communication Configuring Back-Channel Communication Configuring Support for Enhanced Client or Proxy

Trust an identity provider.

For more information, see .

Trusting an Identity Provider

Determine how to federate the identities on the identity provider and service provider.

For more information, see .

Identity Federation

Configure the applications you want to protect with SAML.

For more information, see .

Protecting Resources with SAML

