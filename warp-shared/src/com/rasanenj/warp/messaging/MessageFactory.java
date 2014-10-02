package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class MessageFactory {
    public static Message decode(ByteBuffer msg) {
        long received = System.currentTimeMillis();
        Message.MessageType type = Message.readType(msg);
        long sent = Message.readTimestamp(msg);
        long sendersLastMessageReceived = Message.readTimestamp(msg);
        long sendersLastMessageSent = Message.readTimestamp(msg);
        Message message = null;
        switch (type) {
            case JOIN_SERVER:
                message = new JoinServerMessage(msg);
                break;
            case CHAT_MSG:
                message = new ChatMessage(msg);
                break;
            case DISCONNECT:
                message = new DisconnectMessage(msg);
                break;
            case UPDATE_SHIP_PHYSICS:
                message = new ShipPhysicsMessage(msg);
                break;
            case CREATE_SHIP:
                message = new CreateShipMessage(msg);
                break;
            case SET_ACCELERATION:
                message = new AccelerationMessage(msg);
                break;
            case SHIP_STATS:
                message = new ShipStatsMessage(msg);
                break;
            case SHOOT_REQUEST:
                message = new ShootRequestMessage(msg);
                break;
            case SHOOT_DAMAGE:
                message = new ShootDamageMessage(msg);
                break;
            case SHIP_DESTRUCTION:
                message = new ShipDestructionMessage(msg);
                break;
            case JOIN_CHAT:
                message = new JoinChatMessage(msg);
                break;
            case JOIN_BATTLE:
                message = new JoinBattleMessage(msg);
                break;
            case CREATE_SCORE_GATHERING_POINT:
                message = new ScoreGatheringPointMessage(msg);
                break;
            case SCORE_UPDATE:
                message = new ScoreUpdateMessage(msg);
                break;
            case GAME_STATE_CHANGE:
                message = new GameStateChangeMessage(msg);
                break;
            case DEPLOY_WARNING:
                message = new DeployWarningMessage(msg);
                break;
            case SERVER_UPDATE:
                message = new ServerUpdateMessage(msg);
                break;
            default:
                log(Level.SEVERE, "MessageFactory could not decode type " + type);
        }
        message.getThisStats().setSent(sent);
        message.getThisStats().setReceived(received);
        message.getLastMessageSenderReceivedStats().setReceived(sendersLastMessageReceived);
        message.getLastMessageSenderReceivedStats().setSent(sendersLastMessageSent);

        return message;
    }

}
