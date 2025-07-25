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
package net.sourceforge.plantuml.command;

import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.regex.Matcher2;
import net.sourceforge.plantuml.regex.Pattern2;
import net.sourceforge.plantuml.utils.BlocLines;

public abstract class CommandMultilines<S extends Diagram> implements Command<S> {

	private final Pattern2 starting;
	private final Pattern2 ending;

	public CommandMultilines(Pattern2 starting, Pattern2 ending) {
		// assert patternStart.startsWith("^") && patternStart.endsWith("$");
		this.starting = starting;
		this.ending = ending;
	}

	public final void getPatternEnd() {

	}

	final public CommandControl isValid(BlocLines lines) {
		if (isCommandForbidden())
			return CommandControl.NOT_OK;

		Matcher2 m1 = starting.matcher(lines.getFirst().getTrimmed().getString());
		if (m1.matches() == false)
			return CommandControl.NOT_OK;

		if (lines.size() == 1)
			return CommandControl.OK_PARTIAL;

		m1 = ending.matcher(lines.getLast().getTrimmed().getString());
		if (m1.matches() == false)
			return CommandControl.OK_PARTIAL;

		return finalVerification();
	}

	protected boolean isCommandForbidden() {
		return false;
	}

	protected CommandControl finalVerification() {
		return CommandControl.OK;
	}

	protected final Pattern2 getStartingPattern() {
		return starting;
	}

	protected final Pattern2 getEndingPattern() {
		return ending;
	}

	@Override
	public boolean isEligibleFor(ParserPass pass) {
		return pass == ParserPass.ONE;
	}

}
