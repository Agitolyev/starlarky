/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verygood.security.larky.parser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Larky file that records the children created from it. Useful for collecting dependencies in
 * dry runs.
 */
class CapturingStarFile implements StarFile {
  private final Set<CapturingStarFile> children = new LinkedHashSet<>();
  private final StarFile wrapped;

  CapturingStarFile(StarFile config) {
    this.wrapped = Preconditions.checkNotNull(config);
  }

  @Override
  public StarFile resolve(String path)  {
    CapturingStarFile resolved = new CapturingStarFile(wrapped.resolve(path));
    children.add(resolved);
    return resolved;
  }

  @Override
  public String path() {
    return wrapped.path();
  }

  @Override
  public byte[] readContentBytes() throws IOException {
    return wrapped.readContentBytes();
  }

  @Override
  public String getIdentifier() {
    return wrapped.getIdentifier();
  }

  /**
   * Retrieve collected dependencies.
   * @return A Map mapping the path to the wrapped ConfigFile for each ConfigFile created by this or
   *     one of its descendants. Includes this.
   */
  ImmutableMap<String, StarFile> getAllLoadedFiles() {
    Map<String, StarFile> map = new HashMap<>();
    getAllLoadedFiles(map);
    return ImmutableMap.copyOf(map);
  }

  private void getAllLoadedFiles(Map<String, StarFile> map) {
    map.put(path(), this.wrapped);
    for (CapturingStarFile child : children) {
      child.getAllLoadedFiles(map);
    }
  }

  @Override
  public boolean equals(Object otherObject) {
    if (otherObject instanceof CapturingStarFile) {
      CapturingStarFile other = (CapturingStarFile) otherObject;
      return other.wrapped.equals(this.wrapped) && this.children.equals(other.children);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.path().hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("children", children)
        .add("wrapped", wrapped)
        .toString();
  }
}
