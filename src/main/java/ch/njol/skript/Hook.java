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
package ch.njol.skript;

import java.io.IOException;

import ch.njol.skript.doc.Documentation;
import ch.njol.skript.localization.ArgsMessage;
import dev.ultreon.baseskript.BaseSkript;
import dev.ultreon.baseskript.Plugin;

/**
 * @author Peter Güttinger
 */
public abstract class Hook<P extends Plugin> {
	
	private final static ArgsMessage m_hooked = new ArgsMessage("hooks.hooked"),
			m_hook_error = new ArgsMessage("hooks.error");
	
	public final P plugin;
	
	public final P getPlugin() {
		return plugin;
	}
	
	@SuppressWarnings("null")
	public Hook() throws IOException {
		@SuppressWarnings("unchecked")
		final P p = (P) BaseSkript.getPluginManager().getPlugin(getName());
		plugin = p;
		if (p == null) {
			if (Documentation.canGenerateUnsafeDocs()) {
				loadClasses();
				if (Skript.logHigh())
					Skript.info(m_hooked.toString(getName()));
			}
			return;
		}

		if (!init()) {
			Skript.error(m_hook_error.toString(p.getName()));
			return;
		}

		loadClasses();

		if (Skript.logHigh())
			Skript.info(m_hooked.toString(p.getName()));

    }
	
	protected void loadClasses() throws IOException {
		Skript.getAddonInstance().loadClasses(getClass().getPackage().getName());
	}
	
	/**
	 * @return The hooked plugin's exact name
	 */
	public abstract String getName();
	
	/**
	 * Called when the plugin has been successfully hooked
	 */
	protected boolean init() {
		return true;
	}
	
}
