/**
  * Copyright (c) 2017 ADVA Optical Networking Sp. z o.o.
  * All rights reserved. Any unauthorized disclosure or publication of the confidential and
  * proprietary information to any other party will constitute an infringement of copyright laws.
  *
  * Author: Łukasz Myśliński <LMyslinski@advaoptical.com>
  *
  * Created: 06/10/2017
  */
package core.config

object Environment extends Enumeration{
  val Windows, Unix = Value
}

trait EnvironmentSpecific{
  def pathSeparator(implicit env: Environment.Value) = {
    env match{
      case Environment.Windows => ";"
      case Environment.Unix => ":"
    }
  }
  def dirSeparator(implicit env: Environment.Value) = {
    env match {
      case Environment.Windows => "\\"
      case Environment.Unix => "/"
    }
  }
}