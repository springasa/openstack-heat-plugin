package com.arkea.jenkins.openstack.heat.loader;

import hudson.Extension;
import hudson.model.Descriptor.FormException;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.exception.utils.FormExceptionUtils;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 *         Load Heat Orchestration Template (HOT) from a server HTTP in
 *         format JSON
 */
public class LoaderHttpREST extends AbstractLoader {

	/** Logger. */
	private static Logger LOG = Logger
			.getLogger(LoaderHttpREST.class.getName());

	private String urlHot;

	/**
	 * Files Env activate
	 */
	private boolean checkEnv = false;

	private String urlEnv;

	private String defaultEnv;

	@DataBoundConstructor
	public LoaderHttpREST(String urlHot,
			JSONObject httpRESTEnv) {
		this.urlHot = urlHot;
		if (httpRESTEnv != null) {
			this.checkEnv = true;
			this.urlEnv = ((JSONObject) httpRESTEnv)
					.getString(Constants.URL_ENV);
			this.defaultEnv = ((JSONObject) httpRESTEnv)
					.getString(Constants.DEFAULT_ENV);
		}
	}

	public String getUrlHot() {
		return urlHot;
	}

	public boolean isCheckEnv() {
		return checkEnv;
	}

	public String getUrlEnv() {
		return urlEnv;
	}

	public String getDefaultEnv() {
		return defaultEnv;
	}

	// @Override
	public String[] getHots() {
		return LoaderHttpRESTDescriptor.getListFiles(this.urlHot);
	}

	// @Override
	public String getHot(String hotName) {
		return getFile(this.urlHot, hotName);
	}

	private String getFile(String path, String name) {

		StringBuilder contents = new StringBuilder();

		Scanner scanner = null;
		try {
			scanner = new Scanner(new URL(path + "/" + name).openStream(),
					"UTF-8");

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
		if (Strings.isNullOrEmpty(urlHot)) {
			throw FormExceptionUtils.getFormException(
					Messages.urlHot_label(), Messages.urlHot_name());
		} else if (isCheckEnv()) {
			if (Strings.isNullOrEmpty(urlEnv)) {
				throw FormExceptionUtils
						.getFormException(Messages.urlEnv_label(),
								Messages.urlEnv_name());
			}
		}

		return true;
	}

	@Extension
	public static class LoaderHttpRESTDescriptor extends
			AbstractLoaderDescriptor {

		@Override
		public String getDisplayName() {
			return "LoaderHttpREST";
		}

		/**
		 * Test if the url Hot is valid
		 * 
		 * @param urlHot
		 *            to test
		 * @return the result of the test
		 * @throws IOException
		 *             if the url isn't catched
		 */
		public FormValidation doTestUrlHot(
				@QueryParameter(Constants.URL_HOT) String urlHot)
				throws IOException {

			return doTestUrl(urlHot);
		}

		/**
		 * Test if the url Env is valid
		 * 
		 * @param urlEnv
		 *            to test
		 * @return the result of the test
		 * @throws IOException
		 *             if the url isn't catched
		 */
		public FormValidation doTestUrlEnv(
				@QueryParameter(Constants.URL_ENV) String urlEnv)
				throws IOException {

			return doTestUrl(urlEnv);
		}

		/**
		 * Test if the url is valid
		 * 
		 * @param url
		 *            to test
		 * @return the result of the test
		 * @throws IOException
		 */
		private FormValidation doTestUrl(String url) throws IOException {
			if (Strings.isNullOrEmpty(url)) {
				return FormValidation.warning(Messages.input_filled(Messages
						.urlHot_label()));
			}

			String[] data = getListFiles(url);

			if (data.length == 0) {
				return FormValidation.error(Messages
						.formValidation_errorUrl(url));
			} else {
				return FormValidation.ok(Messages.formValidation_success());
			}
		}

		protected static String[] getListFiles(String url) {
			String[] list = new String[0];
			try {
				list = new ObjectMapper().readValue(new URL(url),
						new TypeReference<String[]>() {
						});
			} catch (IOException e) {
				LOG.log(Level.SEVERE, Messages.file_notFound(url),
						e.fillInStackTrace());
			}
			return list;
		}
	}

	@Override
	public String getFullPathHot(String hotName) {
		return urlHot + "/" + hotName;
	}

	@Override
	public String getFullPathEnv(String envFile) {
		return urlEnv + "/" + envFile;
	}

	@Override
	public String[] getEnvs() {
		return LoaderHttpRESTDescriptor.getListFiles(this.urlEnv);
	}

	@Override
	public String getEnv(String envName) {
		return getFile(this.urlEnv, envName);
	}

	@Override
	public String getDefaultEnvFileName() {
		return urlEnv + "/" + defaultEnv;
	}
}
