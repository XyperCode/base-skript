/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.ultreon.baseskript.BaseSkript;
import dev.ultreon.baseskript.Plugin;

import java.util.Arrays;

@Name("Loaded Plugins")
@Description("An expression to obtain a list of the names of the server's loaded plugins.")
@Examples({
	"if the loaded plugins contains \"Vault\":",
	"\tbroadcast \"This server uses Vault plugin!\"",
	"",
	"send \"Plugins (%size of loaded plugins%): %plugins%\" to player"
})
@Since("2.7")
public class ExprPlugins extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprPlugins.class, String.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] [loaded] plugins");
	}

	@Override
	public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected String @NotNull [] get(@NotNull Object e) {
		return Arrays.stream(BaseSkript.getPluginManager().getPlugins())
			.map(Plugin::getName)
			.toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public @NotNull Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public @NotNull String toString(@Nullable Object e, boolean debug) {
		return "the loaded plugins";
	}

}
