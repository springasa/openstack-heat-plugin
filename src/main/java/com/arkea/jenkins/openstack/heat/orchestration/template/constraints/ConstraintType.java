package com.arkea.jenkins.openstack.heat.orchestration.template.constraints;

import com.arkea.jenkins.openstack.Constants;

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
 *         This class is a enumeration representing the constraint types for a
 *         yaml parameter
 *
 */
public enum ConstraintType {
	allowed_pattern(Constants.CONSTRAINT_ALLOWED_PATTERN), allowed_values(
			Constants.CONSTRAINT_ALLOWED_VALUES), length(
			Constants.CONSTRAINT_LENGTH), range(Constants.CONSTRAINT_RANGE), custom_constraint(
			Constants.CONSTRAINT_CUSTOM_CONSTRAINT);

	private String name;

	private ConstraintType(String name) {
		this.name = name;
	}

	public String getConstraintTypeName() {
		return this.name;
	}
}
