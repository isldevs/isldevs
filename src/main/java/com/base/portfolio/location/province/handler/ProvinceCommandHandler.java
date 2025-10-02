/*
 * Copyright 2025 iSLDevs
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
package com.base.portfolio.location.province.handler;

import com.base.core.command.data.CommandBuilder;
import com.base.portfolio.location.province.controller.ProvinceConstants;

/**
 * @author YISivlay
 */
public class ProvinceCommandHandler extends CommandBuilder {

  public CommandBuilder create() {
    return this.action("CREATE")
        .entity(ProvinceConstants.PERMISSION)
        .href(ProvinceConstants.API_PATH);
  }

  public CommandBuilder update(final Long id) {
    return this.action("UPDATE")
        .entity(ProvinceConstants.PERMISSION)
        .id(id)
        .href(ProvinceConstants.API_PATH + "/" + id);
  }

  public CommandBuilder delete(final Long id) {
    return this.action("DELETE")
        .entity(ProvinceConstants.PERMISSION)
        .id(id)
        .href(ProvinceConstants.API_PATH + "/" + id);
  }
}
