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
/* Generated by Together */

package ghidra.program.util;

import ghidra.framework.options.SaveState;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.listing.Variable;

/**
 * The <CODE>VariableTypeFieldLocation</CODE> class provides specific information
 * about the variable type field within a program location.
 */

public class VariableTypeFieldLocation extends VariableLocation {

	private String typeStr;

	/**
	 * Construct a new VariableTypeFieldLocation object.
	 * @param program the program of the location
	 * @param locationAddr the address of the listing location (i.e., referent code unit)
	 * @param var the variable associated with this type field location.
	 * @param charOffset the position within the function name string for this location.
	 */
	public VariableTypeFieldLocation(Program program, Address locationAddr, Variable var,
			int charOffset) {

		super(program, locationAddr, var, 0, charOffset);
		this.typeStr = var.getDataType().getName();
	}

	/**
	 * Construct a new VariableTypeFieldLocation object.
	 * Variable function entry point is the assumed listing location (i.e., referent code unit).
	 * Care should be taken if variable corresponds to an EXTERNAL function.
	 * @param program the program of the location
	 * @param var the variable associated with this type field location.
	 * @param charOffset the position within the function name string for this location.
	 */
	public VariableTypeFieldLocation(Program program, Variable var, int charOffset) {

		super(program, var, 0, charOffset);
		this.typeStr = var.getDataType().getName();
	}

	/**
	 * Should only be used by XML restoration.
	 */
	public VariableTypeFieldLocation() {
		super();
	}

	/**
	 * Return the function stack variable type string at this location.
	 */
	public String getType() {
		return typeStr;
	}

	/**
	 * Returns a String representation of this location.
	 */
	@Override
	public String toString() {
		return super.toString() + ", Function Variable Type = " + typeStr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((typeStr == null) ? 0 : typeStr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableTypeFieldLocation other = (VariableTypeFieldLocation) obj;
		if (typeStr == null) {
			if (other.typeStr != null)
				return false;
		}
		else if (!typeStr.equals(other.typeStr))
			return false;
		return true;
	}

	@Override
	public void restoreState(Program p, SaveState obj) {
		super.restoreState(p, obj);
		typeStr = obj.getString("_TYPE_STR", "");
	}

	@Override
	public void saveState(SaveState obj) {
		super.saveState(obj);
		obj.putString("_TYPE_STR", typeStr);
	}

}
