package controllers


import javax.inject.Inject

import models.{UserForm, Login}
import models.{UserFormFormat, LoginFormat}


import reactivemongo.api.{ReadPreference, Cursor}
import reactivemongo.play.json.collection.JSONCollection

import utils.{helper, Constants}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.modules.reactivemongo.json.collection._
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.i18n.Messages.Implicits._
import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

import views.html


class Application @Inject()(
                             val reactiveMongoApi: ReactiveMongoApi,
                             val messagesApi: MessagesApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  /*
   * Login Form
   */

  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText)(Login.apply)(Login.unapply))

  /*
   * Registration Form
   */

  val userForm = Form(
    mapping(
      "name" -> nonEmptyText, "email" -> email, "password" -> tuple("main" -> nonEmptyText(Constants.MIN_LENGTH_OF_PASSWORD), "confirm" -> nonEmptyText).
        verifying(Constants.PASSWORD_NOT_MATCHED, passwords => passwords._1 == passwords._2))(UserForm.apply)(UserForm.unapply))


  def index = Action {
    implicit request =>
      Ok(views.html.index("Your new application is ready."))
  }
  /*
   * Login
   */
  def login = Action.async {
    implicit request =>

      Future(Ok(views.html.login(loginForm)))

  }

  /*
 * Logout and clean the session.
 */
  def logout = Action { implicit request =>
    Redirect(routes.Application.login).withNewSession
  }
  /*
  * Redirect Registration Page
    */
  def registration = Action.async {
    implicit request =>
      Future(Ok(views.html.registration(userForm)))

  }
  /*
    * Redirect dashboard
      */
  def dashboard = Action.async { implicit request =>
    if (helper.findSession(request, "email") == " ") {
      Future(Redirect(routes.Application.login))
    } else {
      val jsonuserdata = Json.obj("email" -> helper.findSession(request, "email"))
      val userfutureresult:Future[List[JsObject]] = getDocumentsByQuery(Constants.USER_COLLECTION_NAME, jsonuserdata)

      userfutureresult.map { result =>
        Ok(views.html.dashboard()).withSession("email" -> helper.findSession(request, "email"))
      }

    }
  }

  /*
    * Login Authentication Controller
      */

  def loginAuthentication = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      error => Future(BadRequest(views.html.login(error))),
      loginForm => {
        val email = loginForm.email
        val password = loginForm.password
     val  jsonuserdata = Json.obj("email" -> email, "password" -> password)
        val futureUserdetails:Future[List[JsObject]] = getDocumentsByQuery(Constants.USER_COLLECTION_NAME,jsonuserdata)
        futureUserdetails.map { result =>
          if (result.size > 0) {
            Redirect(routes.Application.login).withSession(
              "email" -> email)
          } else {
            Redirect(routes.Application.login).flashing(
              "message" -> Constants.WRONG_LOGIN_DETAILS, "email" -> email)
          }

        }
      })
  }

  /*
  * user is created after registration
    */

  def createUser = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      error => Future(BadRequest(views.html.registration(error))),
      userForm => {
        val name = userForm.name
        val email = userForm.email
        val (password, _) = userForm.password
        val saveRecord = Json.obj("name" -> name, "email" -> email, "password" -> password)

        val jsonuserdata = Json.obj("email" -> email)
        val futureUserdetails:Future[List[JsObject]] = getDocumentsByQuery(Constants.USER_COLLECTION_NAME, jsonuserdata)

        futureUserdetails.map { result =>
          if (result.size > 0) {
            Redirect(routes.Application.registration).flashing(
              "message" -> Constants.ENTERED_EMAIL_EXISTS, "name" -> name, "email" -> email)
          } else {
            val futuresaveUser = insertDocument(Constants.USER_COLLECTION_NAME, saveRecord)
            Redirect(routes.Application.dashboard).withSession(
              "email" -> email)
          }

        }

      })
  }

  /*
     * Get a JSONCollection (a Collection implementation that is designed to work
     * with JsObject, Reads and Writes.)
     * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
     * the collection reference to avoid potential problems in development with
     * Play hot-reloading.
     */
  def collection(collectionName: String): JSONCollection = {
    db.collection[JSONCollection](collectionName)
  }

  def getDocumentsByQuery(collectionName:String,query:JsObject): Future[List[JsObject]] = {
    val cursor: Cursor[JsObject] = collection(collectionName).find(query).cursor[Login] (ReadPreference.nearest)
    val futureResult: Future[List[JsObject]] = cursor.collect[List]()
    futureResult
  }

  def insertDocument(collectionName: String, documents: JsObject) = {
    collection(collectionName).insert(documents)
  }

}


