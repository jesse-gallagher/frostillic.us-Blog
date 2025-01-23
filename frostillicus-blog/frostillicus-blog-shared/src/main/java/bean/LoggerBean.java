/*
 * Copyright (c) 2012-2025 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bean;

import java.util.logging.Logger;

import darwino.AppDatabaseDef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Provides an app logger for CDI injection.
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@ApplicationScoped
public class LoggerBean {
	@Produces
	public Logger getLogger() {
		return Logger.getLogger(AppDatabaseDef.DATABASE_NAME);
	}
}
