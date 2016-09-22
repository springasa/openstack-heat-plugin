package com.arkea.jenkins.openstack.heat.loader;

import hudson.Extension;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.exception.utils.FormExceptionUtils;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
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
 *         Load Heat Orchestration Template (HOT) from a directory
 *         filesystem
 */
public class LoaderFromDir extends AbstractLoader {

	/** Logger. */
	private static Logger LOG = Logger.getLogger(LoaderFromDir.class.getName());

	/**
	 * path to the directory HOT
	 */
	private String pathHot;

	/**
	 * extension for the Heat Orchestration Template (HOT) File, by default yaml
	 */
	private String extHot = "yaml";

	/**
	 * Files Env activate
	 */
	private boolean checkEnv = false;

	/**
	 * path to the directory Env
	 */
	private String pathEnv;

	/**
	 * extension for the Env File, by default yaml
	 */
	private String extEnv = "yaml";

	/**
	 * Default Env File
	 */
	private String defaultEnv;

	@DataBoundConstructor
	public LoaderFromDir(String pathHot, String extHot, JSONObject fromDirEnv) {

		this.pathHot = pathHot;
		this.extHot = extHot;
		if (fromDirEnv != null) {
			this.checkEnv = true;
			this.pathEnv = ((JSONObject) fromDirEnv).getString(Constants.PATH_ENV);
			this.extEnv = ((JSONObject) fromDirEnv).getString(Constants.EXT_ENV);
			this.defaultEnv = ((JSONObject) fromDirEnv).getString(Constants.DEFAULT_ENV);
		}
	}

	public String getPathHot() {
		return pathHot;
	}

	public String getExtHot() {
		return extHot;
	}

	public boolean isCheckEnv() {
		return checkEnv;
	}

	public String getPathEnv() {
		return pathEnv;
	}

	public String getExtEnv() {
		return extEnv;
	}

	public String getDefaultEnv() {
		return defaultEnv;
	}

	public String getDefaultEnvFileName() {
		return defaultEnv;
	}

	@Override
	public String[] getHots() {
		String[] lst;
		File dir = new File(pathHot);
		lst = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(extHot);
			}
		});
		if (lst == null) {
			lst = new String[0];
		}
		return lst;
	}

	@Override
	public String[] getEnvs() {
		String[] lst;
		File dir = new File(pathEnv);
		lst = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(extEnv);
			}
		});
		if (lst == null) {
			lst = new String[0];
		}
		return lst;
	}

	@Override
	public String getEnv(String envName) {
		return getFile(pathEnv, envName);
	}

	@Override
	public String getHot(String hotName) {
		return getFile(pathHot, hotName);
	}

	private String getFile(String path, String name) {

		StringBuilder contents = new StringBuilder();

		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(path + "/" + name), "UTF-8");

			while (scanner.hasNextLine()) {
				contents.append(scanner.nextLine()).append('\n');
			}

		} catch (IOException e) {
			LOG.log(Level.SEVERE, Messages.file_notFound(path + "/" + name),
					e.fillInStackTrace());
			contents = new StringBuilder();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}

		return contents.toString();
	}

	@Override
	public boolean checkData() throws FormException {

		if (Strings.isNullOrEmpty(pathHot)) {
			throw FormExceptionUtils.getFormException(Messages.pathHot_label(),
					Messages.pathHot_name());
		} else if (Strings.isNullOrEmpty(extHot)) {
			throw FormExceptionUtils.getFormException(Messages.extHot_label(),
					Messages.extHot_name());
		} else if (isCheckEnv()) {
			if (Strings.isNullOrEmpty(pathEnv)) {
				throw FormExceptionUtils.getFormException(
						Messages.pathEnv_label(), Messages.pathEnv_name());
			} else if (Strings.isNullOrEmpty(extEnv)) {
				throw FormExceptionUtils.getFormException(
						Messages.extEnv_label(), Messages.extEnv_name());
			}
		}

		return true;

	}

	@Extension
	public static class LoaderFromDirDescriptor extends
			AbstractLoaderDescriptor {

		@Override
		public String getDisplayName() {
			return "LoaderFromDir";
		}

		/**
		 * Test if the path Hot is valid
		 * 
		 * @param pathHot
		 *            path hot to test
		 * @param extHot
		 *            ext hot to test
		 * @return FormValidation with the message result
		 */
		public FormValidation doTestPathHot(
				@QueryParameter(Constants.PATH_HOT) String pathHot,
				@QueryParameter(Constants.EXT_HOT) String extHot) {

			if (Strings.isNullOrEmpty(pathHot)) {
				return FormValidation.warning(Messages.input_filled(Messages
						.pathHot_label()));
			}

			if (Strings.isNullOrEmpty(extHot)) {
				return FormValidation.warning(Messages.input_filled(Messages
						.extEnv_label()));
			}

			File file = new File(pathHot);
			if (file.isDirectory()) {
				return FormValidation.ok(Messages.formValidation_success());
			} else {
				return FormValidation.error(Messages
						.formValidation_errorDirectory(pathHot));
			}
		}

		/**
		 * Test if the path Env is valid
		 * 
		 * @param pathEnv
		 *            path env to test
		 * @param extEnv
		 *            ext env to test
		 * @param defaultEnv
		 *            default env file to test
		 * @return the result of the test
		 */
		public FormValidation doTestPathEnv(
				@QueryParameter(Constants.PATH_ENV) String pathEnv,
				@QueryParameter(Constants.EXT_ENV) String extEnv,
				@QueryParameter(Constants.DEFAULT_ENV) String defaultEnv) {

			if (Strings.isNullOrEmpty(pathEnv)) {
				return FormValidation.warning(Messages.input_filled(Messages
						.pathEnv_label()));
			}

			if (Strings.isNullOrEmpty(extEnv)) {
				return FormValidation.warning(Messages.input_filled(Messages
						.extEnv_label()));
			}

			File file = new File(pathEnv);
			if (file.isDirectory()) {
				if (!Strings.isNullOrEmpty(defaultEnv)) {
					File fileEnv = new File(pathEnv + "/" + defaultEnv);
					if (!fileEnv.isFile()) {
						return FormValidation.error(Messages
								.formValidation_errorDefaultNotFound(pathEnv,
										defaultEnv));
					}
				}
				return FormValidation.ok(Messages.formValidation_success());
			} else {
				return FormValidation.error(Messages
						.formValidation_errorDirectory(pathEnv));
			}
		}
	}

	@Override
	public String getFullPathHot(String hotName) {
		return pathHot + "/" + hotName;
	}

	@Override
	public String getFullPathEnv(String envFile) {
		return pathEnv + "/" + envFile;
	}

}
