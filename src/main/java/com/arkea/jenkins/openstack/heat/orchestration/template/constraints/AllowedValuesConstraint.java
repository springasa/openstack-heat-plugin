package com.arkea.jenkins.openstack.heat.orchestration.template.constraints;

import java.util.Map;
import java.util.Map.Entry;

import com.arkea.jenkins.openstack.heat.orchestration.template.Parameter;
import com.google.common.base.Strings;

/**
 * @author Credit Mutuel Arkea
 * 
 *         Copyright 2015 Credit Mutuel Arkea
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * 
 *         This class is a bean describing an "allowed_values" type constraint
 *
 */
public class AllowedValuesConstraint extends AbstractConstraint {

	private Map<String, String> allowed_values;

	AllowedValuesConstraint() {
		super(ConstraintType.allowed_values);
	}

	AllowedValuesConstraint(Map<String, String> allowed_values) {
		this();
		this.allowed_values = allowed_values;
	}

	public Map<String, String> getAllowed_values() {
		return allowed_values;
	}

	public void setAllowed_values(Map<String, String> allowed_values) {
		this.allowed_values = allowed_values;
	}

	@Override
	public boolean checkConstraint(Parameter parameter) {
		String testValue = (String) parameter.getDefaultValue();
		if (!Strings.isNullOrEmpty(parameter.getValue())) {
			testValue = parameter.getValue();
		}
		for (Entry<String, String> entry : allowed_values.entrySet()) {
			if (testValue.equals(entry.getValue())) {
				return true;
			}
		}
		return false;
	}

}
