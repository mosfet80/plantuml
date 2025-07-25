/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.sequencediagram.teoz;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.plantuml.klimt.Fashion;
import net.sourceforge.plantuml.klimt.UTranslate;
import net.sourceforge.plantuml.klimt.drawing.UGraphic;
import net.sourceforge.plantuml.klimt.font.StringBounder;
import net.sourceforge.plantuml.sequencediagram.AbstractMessage;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.LifeEvent;
import net.sourceforge.plantuml.sequencediagram.Message;
import net.sourceforge.plantuml.sequencediagram.MessageExo;
import net.sourceforge.plantuml.sequencediagram.Note;
import net.sourceforge.plantuml.sequencediagram.Participant;
import net.sourceforge.plantuml.skin.Context2D;
import net.sourceforge.plantuml.skin.SimpleContext2D;
import net.sourceforge.plantuml.skin.rose.Rose;
import net.sourceforge.plantuml.style.ISkinParam;
import net.sourceforge.plantuml.style.StyleBuilder;

public class LiveBoxes {

	private final Rose skin;
	private final ISkinParam skinParam;
	private final Map<Double, Double> delays = new TreeMap<Double, Double>();
	private final Participant p;
	private final List<Event> events;
	private final Map<Event, Double> eventsStep = new HashMap<Event, Double>();

	public LiveBoxes(Participant p, List<Event> events, Rose skin, ISkinParam skinParam) {
		this.p = p;
		this.events = events;
		this.skin = skin;
		this.skinParam = skinParam;
	}

	public void addStep(Event event, double y) {
		if (event.dealWith(p)) {
			if (event instanceof LifeEvent && ((LifeEvent) event).isDeactivate() && eventsStep.containsValue(y))
				y += 5.0;

			eventsStep.put(event, y);
			event.setY(y);
		}
	}

	public Participant getParticipant() {
		return p;
	}

	public int getLevelAt(Event event, EventsHistoryMode mode) {
		return getLevelAtInternal(event, mode);
	}

	private int getLevelAtInternal(Event event, EventsHistoryMode mode) {
		int level = 0; // p.getInitialLife();
		// System.err.println("--->EventsHistory for " + p + " " + event);
		for (Iterator<Event> it = events.iterator(); it.hasNext();) {
			final Event current = it.next();
			if (current instanceof LifeEvent) {
				final LifeEvent le = (LifeEvent) current;
				if (le.getParticipant() == p && le.isActivate())
					level++;

				if (le.getParticipant() == p && le.isDeactivateOrDestroy())
					level = Math.max(0, level - 1);

			}
			if (event == current) {
				if (current instanceof AbstractMessage) {
					boolean seenActivate = false;
					boolean seenDeactivate = false;
					while (it.hasNext()) {
						final Event next = nextButSkippingNotes(it);
						if (!(next instanceof LifeEvent || next instanceof AbstractMessage))
							break;
						if (!(next instanceof LifeEvent))
							continue;

						final LifeEvent le = (LifeEvent) next;
						final AbstractMessage msg = (AbstractMessage) current;

						final boolean sameMessage = msg == le.getMessage()
								|| (le.getMessage() != null && le.getMessage().isParallelWith(msg));
						if (!sameMessage)
							continue;

						if (mode != EventsHistoryMode.IGNORE_FUTURE_ACTIVATE && le.isActivate() && msg.dealWith(p)
								&& le.getParticipant() == p) {
							seenActivate = true;
							if (seenDeactivate)
								break;
							level++;
						}

						if (mode == EventsHistoryMode.CONSIDERE_FUTURE_DEACTIVATE && le.isDeactivateOrDestroy()
								&& msg.dealWith(p) && le.getParticipant() == p) {
							seenDeactivate = true;
							if (seenActivate)
								break;
							level = Math.max(0, level - 1);
						}

						// System.err.println("Warning, this is message " + current + " next=" + next);
					}

				}
				if (level < 0)
					return 0;

				// System.err.println("<-result1 is " + level);
				return level;
			}
		}
		throw new IllegalArgumentException();
		// return level;
	}

	private boolean isNextEventADestroy(Event event) {
		for (Iterator<Event> it = events.iterator(); it.hasNext();) {
			final Event current = it.next();
			if (event != current)
				continue;

			if (current instanceof Message) {
				final Event next = nextButSkippingNotes(it);
				if (next instanceof LifeEvent) {
					final LifeEvent le = (LifeEvent) next;
					return le.isDestroy(p);
				}
			}
			return false;
		}
		return false;
	}

	private Fashion getActivateColor(Event event) {
		if (event instanceof LifeEvent) {
			final LifeEvent le = (LifeEvent) event;
			if (le.isActivate())
				return le.getSpecificColors();

		}
		for (Iterator<Event> it = events.iterator(); it.hasNext();) {
			final Event current = it.next();
			if (event != current)
				continue;

			if (current instanceof Message || current instanceof MessageExo) {
				Event next = nextButSkippingNotes(it);
				while (next instanceof LifeEvent && ((LifeEvent) next).getMessage() == current) {
					final LifeEvent le = (LifeEvent) next;
					if (le.isActivate() && le.getParticipant() == p)
						return le.getSpecificColors();

					next = nextButSkippingNotes(it);
				}
			}
			return null;
		}
		return null;
	}

	private Event nextButSkippingNotes(Iterator<Event> it) {
		while (true) {
			if (it.hasNext() == false)
				return null;

			final Event next = it.next();
			if (next instanceof Note)
				continue;

			// System.err.println("nextButSkippingNotes=" + next);
			return next;
		}
	}

	public Stairs getStairs(double createY, double totalHeight) {
		final Stairs stair = new Stairs();
		int indent = 0;
		AbstractMessage lastMessage = null;
		Double position = null;
		boolean seenActivate = false;
		boolean seenDeactivate = false;
		for (Event event : events) {
			if (event instanceof Note) {
				// This would be a participant positioned Note, not a Message based Note
				lastMessage = null;
				seenActivate = false;
				seenDeactivate = false;
			}

			final Double potentialPosition = eventsStep.get(event);

			if (position == null || lastMessage == null)
				position = potentialPosition;
			else if (event instanceof LifeEvent) { // && event.dealWith(p)) {
				LifeEvent le = (LifeEvent) event;
				if (le.dealWith(p)) {
					if (le.getMessage() == null || !(le.getMessage().isParallelWith(lastMessage))) {
						position = potentialPosition;
					} else if ((le.isActivate() && seenDeactivate) || (le.isDeactivate() && seenActivate)) {
						position = potentialPosition;
					}
					seenActivate |= le.isActivate();
					seenDeactivate |= le.isDeactivate();
				} else
					continue;
			} else
				position = potentialPosition;

			if (event instanceof AbstractMessage) {
				if (((AbstractMessage) event).isParallelWith(lastMessage))
					continue;
				else {
					seenActivate = false;
					seenDeactivate = false;
					lastMessage = (AbstractMessage) event;
				}
			}

			if (position != null) {
				assert position <= totalHeight : "position=" + position + " totalHeight=" + totalHeight;
				indent = getLevelAt(event, EventsHistoryMode.CONSIDERE_FUTURE_DEACTIVATE);
				final Fashion activateColor = getActivateColor(event);
				StyleBuilder styleBuilder = null;
				if (event instanceof AbstractMessage)
					styleBuilder = ((AbstractMessage) event).getStyleBuilder();
				if (event instanceof LifeEvent)
					styleBuilder = ((LifeEvent) event).getStyleBuilder();
				final Step step = new Step(Math.max(createY, position), isNextEventADestroy(event), indent,
						activateColor, styleBuilder);
				stair.addStep(step);
			}
		}
		stair.addStep(new Step(totalHeight, false, indent, null, null));
		return stair;
	}

	private boolean isActivateAnDeactivate(Event event) {
		if (event instanceof AbstractMessage) {
			final AbstractMessage msg = (AbstractMessage) event;
			return msg.isActivate() && msg.isDeactivate();
		}
		return false;
	}

	public int getMaxValue() {
		int max = 0;
		int level = 0;
		for (Event current : events) {
			if (current instanceof LifeEvent) {
				final LifeEvent le = (LifeEvent) current;
				if (le.getParticipant() == p && le.isActivate())
					level++;

				if (level > max)
					max = level;

				if (le.getParticipant() == p && le.isDeactivateOrDestroy())
					level--;

			}
		}
		return max;
	}

	public double getMaxPosition(StringBounder stringBounder) {
		final int max = getMaxValue();
		final LiveBoxesDrawer drawer = new LiveBoxesDrawer(new SimpleContext2D(true), skin, skinParam, delays,
				p.getStereotype());
		return drawer.getWidth(stringBounder) / 2.0 * max;
	}

	public void drawBoxes(UGraphic ug, Context2D context, double createY, double endY) {
		final Stairs stairs = getStairs(createY, endY);
		final int max = stairs.getMaxIndent();
		if (max == 0)
			drawDestroys(ug, stairs, context);

		for (int i = 1; i <= max; i++)
			drawOneLevel(ug, i, stairs, context);

	}

	private void drawDestroys(UGraphic ug, Stairs stairs, Context2D context) {
		final LiveBoxesDrawer drawer = new LiveBoxesDrawer(context, skin, skinParam, delays, p.getStereotype());
		for (Step yposition : stairs.getSteps())
			drawer.drawDestroyIfNeeded(ug, yposition);

	}

	private void drawOneLevel(UGraphic ug, int levelToDraw, Stairs stairs, Context2D context) {
		final LiveBoxesDrawer drawer = new LiveBoxesDrawer(context, skin, skinParam, delays, p.getStereotype());
		ug = ug.apply(UTranslate.dx((levelToDraw - 1) * drawer.getWidth(ug.getStringBounder()) / 2.0));

		boolean pending = true;
		for (Iterator<Step> it = stairs.getSteps().iterator(); it.hasNext();) {
			final Step yposition = it.next();
			final int indent = yposition.getIndent();
			if (pending && indent == levelToDraw) {
				drawer.addStart(yposition.getValue(), yposition.getColors(), yposition.getStyleBuilder());
				pending = false;
			} else if (pending == false && (it.hasNext() == false || indent < levelToDraw)) {
				drawer.doDrawing(ug, yposition.getValue());
				drawer.drawDestroyIfNeeded(ug, yposition);
				pending = true;
			}
		}
	}

	public void delayOn(double y, double height) {
		delays.put(y, height);
	}

}
