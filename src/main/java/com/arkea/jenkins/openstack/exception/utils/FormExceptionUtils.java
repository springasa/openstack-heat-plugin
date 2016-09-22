package com.arkea.jenkins.openstack.exception.utils;

import com.arkea.jenkins.openstack.heat.i18n.Messages;

import hudson.model.Descriptor.FormException;

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
 *         Class util to manage FormException
 *
 */
public class FormExceptionUtils {

	public static FormException getFormException(String labelField,
			String nameField) {
		return new FormException(Messages.input_wrong(labelField), nameField);
	}

}
