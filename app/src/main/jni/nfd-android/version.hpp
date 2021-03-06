/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2014,  Regents of the University of California,
 *                      Arizona Board of Regents,
 *                      Colorado State University,
 *                      University Pierre & Marie Curie, Sorbonne University,
 *                      Washington University in St. Louis,
 *                      Beijing Institute of Technology,
 *                      The University of Memphis
 *
 * This file is part of NFD (Named Data Networking Forwarding Daemon).
 * See AUTHORS.md for complete list of NFD authors and contributors.
 *
 * NFD is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NFD is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NFD, e.g., in COPYING.md file.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef NFD_VERSION_HPP
#define NFD_VERSION_HPP

namespace nfd {

/** NFD version follows Semantic Versioning 2.0.0 specification
 *  http://semver.org/
 */

// To change version number, modify VERSION variable in top-level wscript.

/** \brief NFD version represented as an integer
 *
 *  MAJOR*1000000 + MINOR*1000 + PATCH
 */
#define NFD_VERSION 4000

/** \brief NFD version represented as a string
 *
 *  MAJOR.MINOR.PATCH
 */
#define NFD_VERSION_STRING "0.4.0"

/** \brief NFD version string, including git commit information, if NFD is build from
 *         specific git commit
 *
 * NFD_VERSION_BUILD_STRING is obtained using the following command (`NFD-` prefix is
 * afterwards removed):
 *
 *    `git describe --match 'NFD-*'`
 *
 * When NFD is built not from git, NFD_VERSION_BUILD_STRING equals NFD_VERSION_STRING
 *
 * MAJOR.MINOR.PATCH(-release-candidate-tag)(-(number-of-commits-since-tag)-COMMIT-HASH)
 *
 * Example, 0.1.0-rc1-1-g5c86570
 */
#define NFD_VERSION_BUILD_STRING "0.4.0-3-g4100646"

/// MAJOR version
#define NFD_VERSION_MAJOR 0
/// MINOR version
#define NFD_VERSION_MINOR 4
/// PATCH version
#define NFD_VERSION_PATCH 0

} // namespace nfd

#endif // NFD_VERSION_HPP
