package $package$.frontend.views.login

import io.udash._
import io.udash.bootstrap.alert.UdashAlert
import io.udash.bootstrap.button.UdashButton
import io.udash.bootstrap.form.UdashForm
import io.udash.bootstrap.tooltip.UdashPopover
import io.udash.bootstrap.utils.BootstrapStyles.Color
import io.udash.bootstrap.utils.ComponentId
import io.udash.bootstrap.utils.UdashIcons.FontAwesome
import io.udash.css._
import io.udash.i18n._
import $package$.frontend.services.TranslationsService
import $package$.shared.css.LoginPageStyles
import $package$.shared.i18n.Translations

class LoginPageView(
  model: ModelProperty[LoginPageModel],
  presenter: LoginPagePresenter,
  translationsService: TranslationsService
) extends FinalView with CssView {

  import scalatags.JsDom.all._
  import translationsService._

  private val errorsAlert = UdashAlert(alertStyle = Color.Danger.toProperty, componentId = ComponentId("alerts"))(
    nested => nested(
      repeat(model.subSeq(_.errors)) { error =>
        div(translatedDynamic(error.get)(_.apply())).render
      }
    )
  )

  private val infoIcon = span(
    LoginPageStyles.infoIcon,
    span(
      FontAwesome.Modifiers.Stack.stack,
      FontAwesome.Modifiers.Sizing.xs,
      span(FontAwesome.Solid.info, FontAwesome.Modifiers.Stack.stack1x),
      span(FontAwesome.Regular.circle, FontAwesome.Modifiers.Stack.stack2x)
    )
  ).render

  // infoIcon - translated popover
  UdashPopover.i18n(content = _ => Translations.Auth.info, trigger = Seq(UdashPopover.Trigger.Hover))(infoIcon)

  private def usernameInput(factory: UdashForm#FormElementsFactory) = {
    factory.input.formGroup()(
      input = _ => factory.input.textInput(model.subProp(_.username), inputId = ComponentId("username"))(
        Some(nested => nested(translatedAttrDynamic(Translations.Auth.usernameFieldPlaceholder, "placeholder")(_.apply())))
      ).render,
      labelContent = Some(nested => Seq[Modifier](nested(translatedDynamic(Translations.Auth.usernameFieldLabel)(_.apply())), " ", infoIcon)),
      invalidFeedback = Some(nested => nested(translatedDynamic(Translations.Auth.emptyInputError)(_.apply()))),
    )
  }

  private def passwordInput(factory: UdashForm#FormElementsFactory) = {
    factory.input.formGroup()(
      input = _ => factory.input.passwordInput(model.subProp(_.password), inputId = ComponentId("password"))(
        Some(nested => nested(translatedAttrDynamic(Translations.Auth.passwordFieldPlaceholder, "placeholder")(_.apply())))
      ).render,
      labelContent = Some(nested => nested(translatedDynamic(Translations.Auth.passwordFieldLabel)(_.apply()))),
      invalidFeedback = Some(nested => nested(translatedDynamic(Translations.Auth.emptyInputError)(_.apply()))),
    )
  }

  // Button from Udash Bootstrap wrapper
  private val disableSubmitBtn = Property(false)
  private val submitButton = UdashButton(
    buttonStyle = Color.Primary.toProperty,
    block = true.toProperty,
    disabled = disableSubmitBtn,
    componentId = ComponentId("login")
  )(nested => Seq[Modifier](nested(translatedDynamic(Translations.Auth.submitButton)(_.apply())), tpe := "submit"))

  // Random permissions notice
  private val permissionsNotice = UdashAlert(alertStyle = Color.Info.toProperty)(
    nested => nested(translatedDynamic(Translations.Auth.randomPermissionsInfo)(_.apply()))
  )

  // disable button when data is invalid
  model.valid.streamTo(disableSubmitBtn, initUpdate = true) {
    case Valid => false
    case _ => true
  }

  private val loginForm = UdashForm(componentId = ComponentId("login-from"))(factory => Seq[Modifier](
    usernameInput(factory),
    passwordInput(factory),

    // submit button or spinner
    showIfElse(model.subProp(_.waitingForResponse))(
      div(
        LoginPageStyles.textCenter,
        span(FontAwesome.Solid.spinner, FontAwesome.Modifiers.Animation.spin, FontAwesome.Modifiers.Sizing.x3)
      ).render,
      submitButton.render
    )
  ))

  loginForm.listen {
    case UdashForm.FormEvent(_, UdashForm.FormEvent.EventType.Submit) =>
      if (!model.subProp(_.waitingForResponse).get) presenter.login()
  }

  def getTemplate: Modifier = div(
    LoginPageStyles.container,

    showIfElse(model.subProp(_.errors).transform(_.nonEmpty))(
      errorsAlert.render,
      permissionsNotice.render
    ),

    loginForm.render
  )
}