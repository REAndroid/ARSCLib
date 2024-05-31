/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.graph;

import com.reandroid.common.DiagnosticMessage;
import com.reandroid.common.DiagnosticsReporter;

public abstract class BaseReporter implements DiagnosticsReporter {

    private DiagnosticsReporter reporter;

    public BaseReporter() {
    }

    public DiagnosticsReporter getReporter() {
        return reporter;
    }
    public void setReporter(DiagnosticsReporter reporter) {
        if(reporter == this) {
            throw new IllegalArgumentException("Can not set reporter to itself");
        }
        this.reporter = reporter;
    }

    public void info(String message) {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            reporter.report(new DiagnosticMessage.StringMessage(
                    DiagnosticMessage.Type.INFO, message));
        }
    }
    public void warn(String message) {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            reporter.report(new DiagnosticMessage.StringMessage(
                    DiagnosticMessage.Type.WARN, message));
        }
    }
    public void verbose(String message) {
        if(isVerboseEnabled()) {
            DiagnosticsReporter reporter = getReporter();
            if(reporter != null) {
                reporter.report(new DiagnosticMessage.StringMessage(
                        DiagnosticMessage.Type.VERBOSE, message));
            }
        }
    }
    public void debug(String message) {
        if(isDebugEnabled()) {
            DiagnosticsReporter reporter = getReporter();
            if(reporter != null) {
                reporter.report(new DiagnosticMessage.StringMessage(
                        DiagnosticMessage.Type.DEBUG, message));
            }
        }
    }
    @Override
    public void report(DiagnosticMessage message) {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            reporter.report(message);
        }
    }
    @Override
    public boolean isVerboseEnabled() {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            return reporter.isVerboseEnabled();
        }
        return false;
    }
    @Override
    public boolean isDebugEnabled() {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            return reporter.isDebugEnabled();
        }
        return false;
    }
    private boolean hasReporter() {
        return getReporter() != null;
    }
}
