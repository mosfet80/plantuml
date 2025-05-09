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
package net.sourceforge.plantuml.activitydiagram3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.plantuml.activitydiagram3.ftile.Ftile;
import net.sourceforge.plantuml.activitydiagram3.ftile.FtileFactory;
import net.sourceforge.plantuml.activitydiagram3.ftile.Swimlane;
import net.sourceforge.plantuml.activitydiagram3.ftile.vcompact.FtileWithNoteOpale;
import net.sourceforge.plantuml.activitydiagram3.gtile.Gtile;
import net.sourceforge.plantuml.activitydiagram3.gtile.GtileSplit;
import net.sourceforge.plantuml.activitydiagram3.gtile.Gtiles;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.decoration.Rainbow;
import net.sourceforge.plantuml.klimt.color.Colors;
import net.sourceforge.plantuml.klimt.creole.Display;
import net.sourceforge.plantuml.klimt.font.StringBounder;
import net.sourceforge.plantuml.klimt.geom.VerticalAlignment;
import net.sourceforge.plantuml.sequencediagram.NotePosition;
import net.sourceforge.plantuml.sequencediagram.NoteType;
import net.sourceforge.plantuml.stereo.Stereotype;
import net.sourceforge.plantuml.style.ISkinParam;

public class InstructionFork extends WithNote implements Instruction {

	private final List<InstructionList> forks = new ArrayList<>();
	private final Instruction parent;
	private final LinkRendering inlinkRendering;
	private final ISkinParam skinParam;
	private final Swimlane swimlaneIn;
	private Swimlane swimlaneOut;
	private ForkStyle style = ForkStyle.FORK;
	private String label;
	boolean finished = false;

	@Override
	public boolean containsBreak() {
		for (InstructionList fork : forks)
			if (fork.containsBreak())
				return true;

		return false;
	}

	public InstructionFork(Instruction parent, LinkRendering inlinkRendering, ISkinParam skinParam, Swimlane swimlane) {
		this.parent = parent;
		this.inlinkRendering = Objects.requireNonNull(inlinkRendering);
		this.skinParam = skinParam;
		this.swimlaneIn = swimlane;
		this.swimlaneOut = swimlane;
		this.forks.add(InstructionList.empty());
	}

	private InstructionList getLastList() {
		return forks.get(forks.size() - 1);
	}

	@Override
	public CommandExecutionResult add(Instruction ins) {
		return getLastList().add(ins);
	}

	// ::comment when __CORE__
	@Override
	public Gtile createGtile(ISkinParam skinParam, StringBounder stringBounder) {
		final List<Gtile> all = new ArrayList<>();
		for (InstructionList list : forks) {
			Gtile tmp = list.createGtile(skinParam, stringBounder);
			tmp = Gtiles.withIncomingArrow(tmp, 20);
			tmp = Gtiles.withOutgoingArrow(tmp, 20);
			all.add(tmp);
		}

		return new GtileSplit(all, swimlaneIn, getInLinkRenderingColor(skinParam).getColor());
	}
	// ::done

	private Rainbow getInLinkRenderingColor(ISkinParam skinParam) {
		Rainbow color;
		color = Rainbow.build(skinParam);
		return color;
	}

	@Override
	public Ftile createFtile(FtileFactory factory) {
		final List<Ftile> all = new ArrayList<>();
		for (InstructionList list : forks)
			all.add(list.createFtile(factory));

		Ftile result = factory.createParallel(all, style, label, swimlaneIn, swimlaneOut);
		if (getPositionedNotes().size() > 0)
			result = FtileWithNoteOpale.create(result, getPositionedNotes(), false, VerticalAlignment.CENTER);

		return result;
	}

	public Instruction getParent() {
		return parent;
	}

	public void forkAgain(Swimlane swimlane) {
		this.swimlaneOut = swimlane;
		this.forks.add(InstructionList.empty());
	}

	@Override
	final public boolean kill() {
		return getLastList().kill();
	}

	@Override
	public LinkRendering getInLinkRendering() {
		return inlinkRendering;
	}

	@Override
	public boolean addNote(Display note, NotePosition position, NoteType type, Colors colors, Swimlane swimlaneNote, Stereotype stereotype) {
		if (finished)
			return super.addNote(note, position, type, colors, swimlaneNote, stereotype);

		if (getLastList().getLast() == null)
			return getLastList().addNote(note, position, type, colors, swimlaneNote, stereotype);

		return getLastList().addNote(note, position, type, colors, swimlaneNote, stereotype);
	}

	@Override
	public Set<Swimlane> getSwimlanes() {
		final Set<Swimlane> result = new HashSet<>(InstructionList.getSwimlanes2(forks));
		result.add(swimlaneIn);
		result.add(swimlaneOut);
		return result;
	}

	@Override
	public Swimlane getSwimlaneIn() {
		return swimlaneIn;
	}

	@Override
	public Swimlane getSwimlaneOut() {
		return swimlaneOut;
	}

	public void manageOutRendering(LinkRendering nextLinkRenderer, boolean endFork) {
		if (endFork)
			this.finished = true;

		if (nextLinkRenderer == null)
			return;

		getLastList().setOutRendering(nextLinkRenderer);
	}

	public void setStyle(ForkStyle style, String label, Swimlane swimlane) {
		this.style = style;
		this.label = label;
		this.swimlaneOut = swimlane;
	}

}
