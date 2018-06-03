/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.mybanchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myserialization.custom.MetaDataLoader;
import com.myserialization.data.Parent;
import com.myserialization.data.TestDataProvider;
import com.myserialization.jnative.NativeSerialization;
import com.myserialization.json.JsonSerialization;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MyBenchmark {

    @State(Scope.Thread)
    public static class InitParams {
        Parent parent;
        byte[] customSerializedObject;
        byte[] jsonSerializedObject;
        byte[] nativeSerializedObject;

        @Setup(Level.Invocation)
        public void setUp() {
            parent = TestDataProvider.getTestData();
            customSerializedObject = MetaDataLoader.serialize(parent);
            try {
                jsonSerializedObject = JsonSerialization.writeJsonObject(parent);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            nativeSerializedObject = NativeSerialization.writeObject(parent);
        }
    }

    @Benchmark
    public void testCustom1Serialization(InitParams ip) {
        MetaDataLoader.serialize(ip.parent);
    }

    @Benchmark
    public Parent testCustom2DeSerialization(InitParams ip) {
        return (Parent) MetaDataLoader.deSerialized(ip.customSerializedObject);
    }

    @Benchmark
    public Parent testJson2DeSerialization(InitParams ip) {
        return (Parent) JsonSerialization.readJsonObject(ip.jsonSerializedObject, ip.parent.getClass());
    }

    @Benchmark
    public void testJson1Serialization(InitParams ip) {
        try {
            JsonSerialization.writeJsonObject(ip.parent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void testNative1Serialization(InitParams ip) {
        NativeSerialization.writeObject(ip.parent);
    }

    @Benchmark
    public Parent testNative2DeSerialization(InitParams ip) {
        return (Parent) NativeSerialization.readObject(ip.nativeSerializedObject);
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .forks(1).warmupIterations(5)
                .build();

        new Runner(opt).run();
    }
}
