Controler: Subject subject = SecurityUtils.getSubject(); UsernamePaswordToken token =newUsernamePaswordToken(username, pasword); subject.login(token);

认证: AuthenticationInfo#doGetAuthenticationInfo AuthenticationToken

<table>
  <tr>
    <th>{<br><br>"credentials": "123", "asword": "123", "principal": "wujinsen", "rememberMe": false, "username": "wujinsen"</th>
  </tr>
</table>


}

SimpleAuthenticationInfo:

<table>
  <tr>
    <th>{<br><br>"cedentials": "123", "principals": {<br><br>"empty": false, "primaryPrincpal": {<br><br>"credentialsSalt": "wujinsenul", "id": 0, "pasword": "123", "username": "wujinsen"<br><br>}, "realmNames": ["com.plumblum.shiro.UserRealm_0"]<br><br>}</th>
  </tr>
</table>


}

授权: AuthorizationInfo#doGetAuthorizationInfo PrincipalColection:

<table>
  <tr>
    <th>{<br><br>"empty": false, "primaryPrincpal": {<br><br>"credentialsSalt": "wujinsenul", "id": 0, "pasword": "123", "username": "wujinsen"<br><br>}, "realmNames": ["com.plumblum.shiro.UserRealm_0"]</th>
  </tr>
</table>


}

SimpleAuthorizationInfo:

<table>
  <tr>
    <th>{<br><br>"roles": ["admin"], "stringPermisions": ["user:create"]</th>
  </tr>
</table>


}

