package $package$.shared.rpc.client

import $package$.shared.rpc.client.chat.ChatNotificationsRPC
import io.udash.rpc._

@RPC
trait MainClientRPC {
  def chat(): ChatNotificationsRPC
}

object MainClientRPC extends DefaultClientUdashRPCFramework.RPCCompanion[MainClientRPC]