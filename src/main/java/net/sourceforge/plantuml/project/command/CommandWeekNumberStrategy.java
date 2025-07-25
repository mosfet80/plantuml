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
package net.sourceforge.plantuml.project.command;

import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.ParserPass;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.project.GanttDiagram;
import net.sourceforge.plantuml.project.time.DayOfWeek;
import net.sourceforge.plantuml.regex.IRegex;
import net.sourceforge.plantuml.regex.RegexConcat;
import net.sourceforge.plantuml.regex.RegexLeaf;
import net.sourceforge.plantuml.regex.RegexResult;
import net.sourceforge.plantuml.utils.LineLocation;

public class CommandWeekNumberStrategy extends SingleLineCommand2<GanttDiagram> {

	public CommandWeekNumberStrategy() {
		super(getRegexConcat());
	}

	static IRegex getRegexConcat() {
		return RegexConcat.build(CommandWeekNumberStrategy.class.getName(), RegexLeaf.start(), //
				new RegexLeaf("weeks?"), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf("starts?"), //
				new RegexLeaf("[^0-9]*?"), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf(1, "WEEKDAY", "(" + DayOfWeek.getRegexString() + ")"), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf("[^0-9]*?"), //
				new RegexLeaf(1, "NUM", "([0-9]+)"), //
				new RegexLeaf("[^0-9]*?"), //
				RegexLeaf.end());
	}

	@Override
	protected CommandExecutionResult executeArg(GanttDiagram diagram, LineLocation location, RegexResult arg, ParserPass currentPass) {

		final DayOfWeek weekDay = DayOfWeek.fromString(arg.get("WEEKDAY", 0));
		final String num = arg.get("NUM", 0);
		diagram.setWeekNumberStrategy(weekDay, Integer.parseInt(num));
		return CommandExecutionResult.ok();
	}

}
