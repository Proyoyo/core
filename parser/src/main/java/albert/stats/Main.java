package albert.stats;
import skadistats.clarity.model.Entity;
import skadistats.clarity.Clarity;
import skadistats.clarity.match.Match;
import skadistats.clarity.parser.Profile;
import skadistats.clarity.parser.TickIterator;
import skadistats.clarity.model.UserMessage;
import skadistats.clarity.model.GameEvent;
import skadistats.clarity.model.GameEventDescriptor;
import skadistats.clarity.model.GameRulesStateType;
import com.dota2.proto.DotaUsermessages.DOTA_COMBATLOG_TYPES;
import com.dota2.proto.Demo.CDemoFileInfo;
import org.json.JSONObject;
import org.json.JSONArray;

public class Main {
    public static final float INTERVAL = 60;
    public static final String[] PLAYER_IDS = {"0000","0001","0002","0003","0004","0005","0006","0007","0008","0009"};

    public static void main(String[] args) throws Exception {

        long tStart = System.currentTimeMillis();

        boolean initialized = false;
        GameEventDescriptor combatLogDescriptor = null;
        JSONObject doc = new JSONObject();
        Match match = new Match();
        TickIterator iter = Clarity.tickIteratorForFile(args[0], Profile.ALL);
        float nextInterval = INTERVAL;

        while(iter.hasNext()) {
            iter.next().apply(match);
            int gameTime = (int) match.getGameTime();
            String time = String.valueOf(gameTime);

            if (!initialized) {
                doc.put("players", new JSONArray());
                doc.put("combatlog", new JSONObject());
                doc.put("times", new JSONArray());

                Entity pr = match.getPlayerResource();

                for (int i = 0; i < PLAYER_IDS.length; i++) {
                    JSONObject player = new JSONObject();
                    player.put("display_name", pr.getProperty("m_iszPlayerNames" + "." + PLAYER_IDS[i]));
                    player.put("steamid", pr.getProperty("m_iPlayerSteamIDs" + "." + PLAYER_IDS[i]));
                    player.put("last_hits", new JSONArray());
                    player.put("gold", new JSONArray());
                    player.put("xp", new JSONArray());
                    player.put("buybacks", new JSONArray());
                    player.put("kills", new JSONArray());
                    player.put("runes", new JSONArray());
                    player.put("purchases", new JSONArray());
                    player.put("glyphs", new JSONArray());
                    player.put("courier_kills", new JSONArray());
                    player.put("aegis", new JSONArray());
                    player.put("pauses", new JSONArray());
                    doc.getJSONArray("players").put(player);
                }
                combatLogDescriptor = match.getGameEventDescriptors().forName("dota_combatlog"); 
                CombatLogEntry.init(
                    match.getStringTables().forName("CombatLogNames"), 
                    combatLogDescriptor
                );
                initialized = true;
            }
            for (UserMessage u : match.getUserMessages()) {
                if (u.getName().startsWith("CDOTAUserMsg_ChatEvent")){
                    JSONArray players = doc.getJSONArray("players");
                    int player1=(int)u.getProperty("playerid_1");
                    int player2=(int)u.getProperty("playerid_2");
                    String type = u.getProperty("type").toString();

                    if (type.contains("RUNE")){
                        players.getJSONObject(player1).getJSONArray("runes").put(u.getProperty("value"));
                    }
                    else if (type.contains("ITEM_PURCHASE")){
                    }
                    else if (type.contains("GLYPH")){
                        players.getJSONObject(player1).getJSONArray("glyphs").put(time);
                    }
                    else if (type.contains("BUYBACK")){
                        players.getJSONObject(player1).getJSONArray("buybacks").put(time);
                    }
                    else if (type.contains("CONNECT")){
                    }
                    else if (type.contains("TOWER_KILL")){
                    }
                    else if (type.contains("BARRACKS_KILL")){
                    }
                    else if (type.contains("COURIER_LOST")){
                        players.getJSONObject(player1).getJSONArray("courier_kills").put(time);
                    }
                    else if (type.contains("ROSHAN_KILL")){
                    }
                    else if (type.contains("AEGIS")){
                        players.getJSONObject(player1).getJSONArray("aegis").put(time);
                    }
                    else if (type.contains("PAUSE")){
                        if (type.contains("_PAUSED")){
                            players.getJSONObject(player1).getJSONArray("pauses").put(time);
                        }
                    }
                    else if (type.contains("HERO_KILL")){
                        if (player2>0 && player2<10){
                            players.getJSONObject(player2).getJSONArray("kills").put(player1);
                        }
                    }
                    else if (type.contains("STREAK_KILL")){ 
                    }
                    else{
                        System.err.println(u);  
                    }
                }
            }
            for (GameEvent g : match.getGameEvents()) {
                //dewards
                //distance traveled
                if (g.getEventId() == combatLogDescriptor.getEventId()) {
                    CombatLogEntry cle = new CombatLogEntry(g);
                    JSONObject combatlog=doc.getJSONObject("combatlog");
                    String hero;
                    String item;
                    switch(cle.getType()) {
                        case 0:
                        /*
                    System.out.format("{} {} hits {}{} for {} damage{}", 
                             time, 
                             cle.getAttackerNameCompiled(),
                             cle.getTargetNameCompiled(), 
                             cle.getInflictorName() != null ? String.format(" with %s", cle.getInflictorName()) : "",
                             cle.getValue(),
                             cle.getHealth() != 0 ? String.format(" (%s->%s)", cle.getHealth() + cle.getValue(), cle.getHealth()) : ""
                            );
                            */
                        break;
                        case 1:
                        /*
                    System.out.format("{} {}'s {} heals {} for {} health ({}->{})", 

                             time, 

                             cle.getAttackerNameCompiled(), 

                             cle.getInflictorName(), 

                             cle.getTargetNameCompiled(), 

                             cle.getValue(), 

                             cle.getHealth() - cle.getValue(), 

                             cle.getHealth()

                            );

                            */
                        break;
                        case 2:
                        //look in user message log for runes?
                        /*
                    System.out.format("{} {} receives {} buff/debuff from {}", 
                             time, 
                             cle.getTargetNameCompiled(), 
                             cle.getInflictorName(), 
                             cle.getAttackerNameCompiled()
                            );
                            */
                        break;
                        case 3:
                        /*
                    System.out.format("{} {} loses {} buff/debuff", 
                             time, 
                             cle.getTargetNameCompiled(), 
                             cle.getInflictorName()
                            );
                            */
                        break;
                        case 4:
                        //System.out.format("[KILL] %s, %s%n",cle.getAttackerName(),cle.getTargetName());
                        break;
                        case 5:
                        /*
                        JSONObject abilityEntry = new JSONObject();
                        abilityEntry.put("time", time);
                        abilityEntry.put("name", cle.getInflictorName());
                        insertHero(combatlog, cle.getAttackerName(), "abilities", abilityEntry);
                        */
                        break;
                        case 6:
                        hero = cle.getAttackerName();
                        item = cle.getInflictorName();
                        if(!combatlog.has(hero)){
                            JSONObject heroObject = new JSONObject();
                            heroObject.put("purchases", new JSONArray());
                            heroObject.put("uses", new JSONObject());
                            combatlog.put(hero, heroObject);
                        }
                        JSONObject counts = combatlog.getJSONObject(hero).getJSONObject("uses");
                        Integer count = counts.has(item) ? (Integer)counts.get(item) : 0;
                        counts.put(item, count + 1);
                        break;
                        case 8:
                        /*
                    System.out.format("{} {} {} {} gold", 
                             time, 
                             cle.getTargetNameCompiled(),
                             cle.getValue() < 0 ? "loses" : "receives",
                             Math.abs(cle.getValue())
                            );
                            */
                        break;
                        case 9:
                        //game state
                        //System.out.format("[STATE] %s%n", GameRulesStateType.values()[cle.getValue() - 1]); 
                        break;
                        case 10:
                        /*
                         System.out.format("{} {} gains {} XP", 
                             time, 
                             cle.getTargetNameCompiled(),
                             cle.getValue()
                            );
                            */
                        break;
                        case 11:
                        hero = cle.getTargetName();
                        item = cle.getValueName();
                        if(!combatlog.has(hero)){
                            JSONObject heroObject = new JSONObject();
                            heroObject.put("purchases", new JSONArray());
                            heroObject.put("uses", new JSONObject());
                            combatlog.put(hero, heroObject);
                        }
                        if (!item.contains("recipe")){
                            JSONObject buyEntry = new JSONObject();
                            buyEntry.put("time", time);
                            buyEntry.put("name", item);
                            combatlog.getJSONObject(hero).getJSONArray("purchases").put(buyEntry);     
                        }
                        break;
                        case 12:
                        //doc.getJSONArray("players").getJSONObject(cle.getValue()).getJSONArray("buybacks").put(time);
                        break;
                        default:
                        DOTA_COMBATLOG_TYPES type = DOTA_COMBATLOG_TYPES.valueOf(cle.getType());
                        System.err.format("%s (%s): %s%n", type.name(), type.ordinal(), g);
                        break;
                    }

                }

            }

            if (gameTime > nextInterval) {
                Entity pr = match.getPlayerResource();
                doc.getJSONArray("times").put(time);

                for (int i = 0; i < PLAYER_IDS.length; i++) {
                    JSONObject player = doc.getJSONArray("players").getJSONObject(i);
                    //todo multiple heroes for ARDM, could also update more than once a min
                    player.put("hero", pr.getProperty("m_nSelectedHeroID" + "." + PLAYER_IDS[i]));
                    player.getJSONArray("last_hits").put(pr.getProperty("m_iLastHitCount" + "." + PLAYER_IDS[i]));
                    player.getJSONArray("xp").put(pr.getProperty("EndScoreAndSpectatorStats.m_iTotalEarnedXP" + "." + PLAYER_IDS[i]));
                    player.getJSONArray("gold").put(pr.getProperty("EndScoreAndSpectatorStats.m_iTotalEarnedGold" + "." + PLAYER_IDS[i]));
                }
                nextInterval += INTERVAL;
            }
        }

        System.out.println(doc);
        long tMatch = System.currentTimeMillis() - tStart;
        System.err.format("time: %s sec%n", tMatch / 1000.0);      
    }

}
