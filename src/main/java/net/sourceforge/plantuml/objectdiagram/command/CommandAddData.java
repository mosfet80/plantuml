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
package net.sourceforge.plantuml.objectdiagram.command;

import net.sourceforge.plantuml.abel.Entity;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.ParserPass;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.klimt.color.NoSuchColorException;
import net.sourceforge.plantuml.objectdiagram.AbstractClassOrObjectDiagram;
import net.sourceforge.plantuml.plasma.Quark;
import net.sourceforge.plantuml.regex.IRegex;
import net.sourceforge.plantuml.regex.RegexConcat;
import net.sourceforge.plantuml.regex.RegexLeaf;
import net.sourceforge.plantuml.regex.RegexResult;
import net.sourceforge.plantuml.skin.VisibilityModifier;
import net.sourceforge.plantuml.utils.LineLocation;

public class CommandAddData extends SingleLineCommand2<AbstractClassOrObjectDiagram> {
    // ::remove folder when __HAXE__

	public CommandAddData() {
		super(getRegexConcat());
	}

	static IRegex getRegexConcat() {
		return RegexConcat.build(CommandAddData.class.getName(), RegexLeaf.start(), //
				new RegexLeaf(1, "NAME", "([%pLN_.]+)"), //
				RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf(":"), //
				RegexLeaf.spaceZeroOrMore(), //
				new RegexLeaf(1, "DATA", "(.*)"), RegexLeaf.end()); //
	}

	@Override
	protected CommandExecutionResult executeArg(AbstractClassOrObjectDiagram diagram, LineLocation location,
			RegexResult arg, ParserPass currentPass) throws NoSuchColorException {
		final String name = arg.get("NAME", 0);
		final Quark<Entity> quark = diagram.quarkInContext(true, diagram.cleanId(name));
		final Entity entity = quark.getData();
		if (entity == null)
			return CommandExecutionResult.error("No such entity " + quark.getName());

		final String field = arg.get("DATA", 0);
		if (field.length() > 0 && VisibilityModifier.isVisibilityCharacter(field))
			diagram.setVisibilityModifierPresent(true);

		entity.getBodier().addFieldOrMethod(field);
		return CommandExecutionResult.ok();
	}
}
