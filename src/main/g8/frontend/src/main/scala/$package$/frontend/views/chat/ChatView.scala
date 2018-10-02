package $package$.frontend.views.chat

import io.udash._
import io.udash.bootstrap.button.UdashButton
import io.udash.bootstrap.card.UdashCard
import io.udash.bootstrap.form.{UdashForm, UdashInputGroup}
import io.udash.bootstrap.utils.BootstrapStyles.Color
import io.udash.bootstrap.utils.ComponentId
import io.udash.bootstrap.utils.UdashIcons.FontAwesome
import io.udash.css._
import io.udash.i18n._
import $package$.frontend.services.TranslationsService
import $package$.shared.css.ChatStyles
import $package$.shared.i18n.Translations

class ChatView(model: ModelProperty[ChatModel], presenter: ChatPresenter, translationsService: TranslationsService)
  extends FinalView with CssView {

  import scalatags.JsDom.all._
  import translationsService._

  private val messagesWindow = div(
    ChatStyles.messagesWindow,
    repeat(model.subSeq(_.msgs)) { msgProperty =>
      val msg = msgProperty.get
      div(
        ChatStyles.msgContainer,
        strong(msg.author, ": "),
        span(msg.text),
        span(ChatStyles.msgDate, msg.created.toString)
      ).render
    }
  )

  // Standard Udash TextInput (we don't need Bootstrap Forms input wrapping)
  private val msgInput = TextInput(model.subProp(_.msgInput))(
    translatedAttrDynamic(Translations.Chat.inputPlaceholder, "placeholder")(_.apply())
  )

  // Button from Udash Bootstrap wrapper
  private val submitButton = UdashButton(
    buttonStyle = Color.Primary.toProperty,
    block = true.toProperty,
    componentId = ComponentId("send")
  )(_ => Seq[Modifier](span(FontAwesome.Brands.telegramPlane), tpe := "submit"))

  private val msgForm = UdashForm(componentId = ComponentId("msg-from"))(factory => Seq[Modifier](
    // disable form if user don't has write access
    factory.disabled(Property(!presenter.hasWriteAccess)) { nested =>
      nested(
        UdashInputGroup()(
          UdashInputGroup.input(msgInput.render),
          UdashInputGroup.appendButton(submitButton.render)
        )
      )
    }
  ))

  msgForm.listen {
    case UdashForm.FormEvent(_, UdashForm.FormEvent.EventType.Submit) =>
      presenter.sendMsg()
  }

  override def getTemplate: Modifier = div(
    UdashCard(componentId = ComponentId("chat-panel"))(factory => Seq[Modifier](
      factory.header(nested =>
        nested(
          produce(model.subProp(_.connectionsCount)) { count =>
            span(translatedDynamic(Translations.Chat.connections)(_.apply(count))).render
          }
        )
      ),
      factory.body(_ => messagesWindow),
      factory.footer(_ => msgForm)
    )).render
  )
}
