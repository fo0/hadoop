/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.yarn.service.client.params;

import com.beust.jcommander.Parameter;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.service.exceptions.BadCommandArgumentsException;
import org.apache.hadoop.yarn.service.exceptions.ErrorStrings;
import org.apache.hadoop.yarn.service.exceptions.UsageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base args for all actions
 */
public abstract class AbstractActionArgs extends ArgOps implements Arguments {
  protected static final Logger log =
    LoggerFactory.getLogger(AbstractActionArgs.class);


  protected AbstractActionArgs() {
  }

  /**
   * This is the default parameter
   */
  @Parameter
  public List<String> parameters = new ArrayList<>();

  /**
   * get the name: relies on arg 1 being the cluster name in all operations 
   * @return the name argument, null if there is none
   */
  public String getClusterName() {
    return (parameters.isEmpty()) ? null : parameters.get(0);
  }

  /**
   -D name=value

   Define an configuration option which overrides any options in
   the configuration XML files of the image or in the image configuration
   directory. The values will be persisted.
   Configuration options are only passed to the cluster when creating or reconfiguring a cluster.

   */

  @Parameter(names = ARG_DEFINE, arity = 1, description = "Definitions", hidden = true)
  public List<String> definitions = new ArrayList<>();

  /**
   * System properties
   */
  @Parameter(names = {ARG_SYSPROP}, arity = 1,
             description = "system properties in the form name value" +
                           " These are set after the JVM is started.",
              hidden = true)
  public List<String> sysprops = new ArrayList<>(0);


  @Parameter(names = ARG_DEBUG, description = "Debug mode", hidden = true)
  public boolean debug = false;

  /**
   * Get the min #of params expected
   * @return the min number of params in the {@link #parameters} field
   */
  public int getMinParams() {
    return 1;
  }

  /**
   * Get the name of the action
   * @return the action name
   */
  public abstract String getActionName() ;

  /**
   * Get the max #of params expected
   * @return the number of params in the {@link #parameters} field;
   */
  public int getMaxParams() {
    return getMinParams();
  }

  public void validate() throws BadCommandArgumentsException, UsageException {
    
    int minArgs = getMinParams();
    int actionArgSize = parameters.size();
    if (minArgs > actionArgSize) {
      throw new BadCommandArgumentsException(
        ErrorStrings.ERROR_NOT_ENOUGH_ARGUMENTS + getActionName() +
        ", Expected minimum " + minArgs + " but got " + actionArgSize);
    }
    int maxArgs = getMaxParams();
    if (maxArgs == -1) {
      maxArgs = minArgs;
    }
    if (actionArgSize > maxArgs) {
      String message = String.format("%s for action %s: limit is %d but saw %d: ",
                                     ErrorStrings.ERROR_TOO_MANY_ARGUMENTS,
                                     getActionName(), maxArgs,
                                     actionArgSize);
      
      log.error(message);
      int index = 1;
      StringBuilder buf = new StringBuilder(message);
      for (String actionArg : parameters) {
        log.error("[{}] \"{}\"", index++, actionArg);
        buf.append(" \"").append(actionArg).append("\" ");
      }
      throw new BadCommandArgumentsException(buf.toString());
    }
  }

  @Override
  public String toString() {
    return super.toString() + ": " + getActionName();
  }
}
