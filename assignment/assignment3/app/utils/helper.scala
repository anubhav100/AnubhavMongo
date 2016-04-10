package utils

import play.api.mvc.RequestHeader
/**
  * Created by Anubhav
  */
object helper {

  /*
   * find session values
   * @param request
   * @param element - find session value stored in element
   */

  def findSession(request:RequestHeader,element:String):String ={
    request.session.get(element).map(sessionValue =>sessionValue)}.getOrElse{
    " "
  }

}
