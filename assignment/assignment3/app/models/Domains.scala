package models


import play.api.libs.json.Json

/**
  * Created by knoldus on 8/4/16.
  */

case class Login(email: String, password: String)

case class UserForm(name: String, email: String, password: (String, String))

object LoginFormat {


  // Generates Writes and Reads
  implicit val loginFormat = Json.format[Login]
}

object UserFormFormat {


  // Generates Writes and Reads
  implicit val userFormFormat = Json.format[Login]
}










