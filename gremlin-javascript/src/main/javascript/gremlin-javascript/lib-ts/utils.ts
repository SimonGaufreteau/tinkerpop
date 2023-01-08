/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * A module containing any utility functions.
 * @author Jorge Bay Gondra
 */
import crypto from 'crypto';
import os from 'os';
import { Element } from './structure/Element';
const gremlinVersion = require(__dirname + '/../package.json').version;

export function getUuid() {
  const buffer = crypto.randomBytes(16);
  //clear the version
  buffer[6] &= 0x0f;
  //set the version 4
  buffer[6] |= 0x40;
  //clear the variant
  buffer[8] &= 0x3f;
  //set the IETF variant
  buffer[8] |= 0x80;
  const hex = buffer.toString('hex');
  return (
    hex.substr(0, 8) +
    '-' +
    hex.substr(8, 4) +
    '-' +
    hex.substr(12, 4) +
    '-' +
    hex.substr(16, 4) +
    '-' +
    hex.substr(20, 12)
  );
}

export const emptyArray = Object.freeze([]);

function generateUserAgent() {
  const applicationName = (process.env.npm_package_name || 'NotAvailable').replace('_', ' ');
  const driverVersion = gremlinVersion.replace('_', ' ');
  const runtimeVersion = process.version.replace(' ', '_');
  const osName = os.platform().replace(' ', '_');
  const osVersion = os.release().replace(' ', '_');
  const cpuArch = process.arch.replace(' ', '_');
  const userAgent = `${applicationName} Gremlin-Javascript.${driverVersion} ${runtimeVersion} ${osName}.${osVersion} ${cpuArch}`;

  return userAgent;
}

export function getUserAgentHeader() {
  return 'User-Agent';
}

const userAgent = generateUserAgent();

export function getUserAgent() {
  return userAgent;
}

// TSTODO : Summarize type ?
export function summarize(value: any | undefined | null) {
  if (!value) {
    return value;
  }

  const strValue = value.toString();
  return strValue.length > 20 ? strValue.substr(0, 20) : strValue;
}

export function areEqual(obj1: any, obj2: any): boolean {
  if (obj1 === obj2) {
    return true;
  }
  if (typeof obj1.equals === 'function') {
    return obj1.equals(obj2);
  }
  if (Array.isArray(obj1) && Array.isArray(obj2)) {
    if (obj1.length !== obj2.length) {
      return false;
    }
    for (let i = 0; i < obj1.length; i++) {
      if (!areEqual(obj1[i], obj2[i])) {
        return false;
      }
    }
    return true;
  }
  return false;
}
