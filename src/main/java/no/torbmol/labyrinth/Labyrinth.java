/* Copyright 2019 Torbjørn Birch Moltu
 *
 * This program is licensed under the GNU General Public License, as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package no.torbmol.labyrinth;
import static java.awt.Color.*;
import static java.awt.event.KeyEvent.*;
import static no.torbmol.util.geom.Direction.*;
import java.io.File;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import no.torbmol.util.awtKeyListen;
import no.torbmol.util.geom.Direction;
import java.util.LinkedList;
import no.torbmol.labyrinth.method.*;

public class Labyrinth {
	public static void main(String[] args) {
		Window.start("Labyrinth");
		Type.add("wall",   true , false, "wall.png",   BLACK,  "#");
		Type.add("floor",  false, false, "floor.png",  WHITE,  " ");
		Type.add("exit",   false, false, "exit.png", MAGENTA,  "-");
		Type.add("start",  false, false, "start.png",  GREEN,  "*");
		Type.add("button", true , true , "button.png",   RED,  "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Type.add("plate",  false, true , "plate.png", ORANGE,  "abcdefghijklmnopqrstuvwxyz");
		Type.add("portal", false, true , "portal.png",  BLUE,  "0123456789");
		Type.add("dot",    false, false, "dot.png",   YELLOW,  ".");
		Type.add("enemy",  false, false, null,           WHITE,  "!");
		Type.add("hammer", false, false, "hammer.png", WHITE,  "^");

		if (args.length == 1) {
			MapFile.read(new File(args[0]));
		} else {//show a fileChooser
			MapFile.read(MapFile.choose("src/main/resources/maps/"));
		}
		TileMap.all("enemy", tile -> {
			tile.setType("floor");
			new Enemy.Normal(tile, "enemy.png", 600, 0, 600);
		});

		TileMap.all("exit", t->t.visible());

		Player p = new Player("player.png", player->{
			if (player.tile().isType("exit")) {
				player.tile().leave(true);
				Window.won();
			}
			else if (player.tile().isType("hammer")) {
				player.tile().setType("floor");
				player.hammer(5000);
			}
			else if (player.tile().isType("dot")) {
				player.tile().setType("floor");
				boolean last = !TileMap.anyTiles("dot");
				Scope.Variable f = Script.scr.root.get_variable("onDot");
				f.getRef().call(List.of(Value.VBool.v(last)));
			}
		});
		Window.display();
		Tile start = findStart(p);
		p.moveTo( start );
		var afterInit = Script.scr.root.get_variable("start");
		if (afterInit != null) {
			afterInit.getRef().call(List.of());
		}
		//see in all directions, except NONE
		for (Direction d : new Direction[]{NORTH, SOUTH, EAST, WEST}) {
			LoS.triangle(p.tile().pos(), d, t -> t.visible());
		}
		p.start();
		Mob.pauseAll(false);
	}



	private static Tile findStart(Player player) {
		LinkedList<Tile> start = new LinkedList<>();
		TileMap.all("start", tile-> {
			if (tile.mob()==null) {
				start.add(tile);
				tile.visible();
			}
		});
		if (start.isEmpty()) {
			throw Window.error("No start tile.");
		}
		if (start.size() == 1) {
			return start.get(0);
		}

		LinkedTransferQueue<KeyEvent> queue = new LinkedTransferQueue<>();
		awtKeyListen.Pressed klp = event->queue.add(event);
		Window.window.addKeyListener(klp);
		start.get(0).enter(player, false);

		int index = 0;
		boolean finished = false;
		while (!finished) {
			try {
				switch (queue.take().getKeyCode()) {
					case VK_LEFT:
						start.get(index).leave(false);
						if (index == 0) {
							index = start.size();
						}
						index--;
						start.get(index).enter(player, false);
						break;
					case VK_RIGHT:
						start.get(index).leave(false);
						index++;
						if (index == start.size()) {
							index = 0;
						}
						start.get(index).enter(player, false);
						break;
					case VK_ENTER:
						finished = true;
				}
			} catch (InterruptedException e1) {
				finished = true;
			}
		}
		Window.window.removeKeyListener(klp);
		start.get(index).leave(false);
		return start.get(index);
	}
}
