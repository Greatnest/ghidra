/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package agent.gdb.manager.evt;

import agent.gdb.manager.breakpoint.GdbBreakpointInfo;
import agent.gdb.manager.impl.GdbManagerImpl;
import agent.gdb.manager.parsing.GdbParsingUtils.GdbParseError;

/**
 * The event corresponding with "{@code =breakpoint-created}"
 */
public class GdbBreakpointCreatedEvent extends AbstractGdbEventWithFields {
	private final GdbBreakpointInfo bkptInfo;

	/**
	 * Construct a new event by parsing the tail for information
	 * 
	 * The breakpoint information must be specified by GDB.
	 * 
	 * @param tail the text following the event type in the GDB/MI event record
	 * @throws GdbParseError if the tail cannot be parsed
	 */
	public GdbBreakpointCreatedEvent(CharSequence tail, GdbManagerImpl manager)
			throws GdbParseError {
		super(tail);
		this.bkptInfo = GdbBreakpointInfo.parse(getInfo(), manager.currentInferior().getId());
	}

	/**
	 * Get the breakpoint information
	 * 
	 * @return the parsed, but not processed, breakpoint information
	 */
	public GdbBreakpointInfo getBreakpointInfo() {
		return bkptInfo;
	}
}
