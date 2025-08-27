package com.fitmatch.common.enums;

import java.util.*;

public enum Activity {

  // ENDURANCE
  RUNNING(ActivityCategory.ENDURANCE),
  CYCLING(ActivityCategory.ENDURANCE),
  SWIMMING(ActivityCategory.ENDURANCE),
  WALKING(ActivityCategory.ENDURANCE),
  ROWING(ActivityCategory.ENDURANCE),
  SPINNING(ActivityCategory.ENDURANCE),

  // FLEXIBILITY
  YOGA(ActivityCategory.FLEXIBILITY),
  PILATES(ActivityCategory.FLEXIBILITY),

  // STRENGTH / CONDITIONING
  STRENGTH_TRAINING(ActivityCategory.STRENGTH),
  HIIT(ActivityCategory.STRENGTH),

  // TEAM SPORTS
  FOOTBALL(ActivityCategory.TEAM_SPORT),
  BASKETBALL(ActivityCategory.TEAM_SPORT),

  // OUTDOOR
  HIKING(ActivityCategory.OUTDOOR),
  CLIMBING(ActivityCategory.OUTDOOR);

  private final ActivityCategory category;

  Activity(ActivityCategory category) {
    this.category = Objects.requireNonNull(category);
  }

  public ActivityCategory category() {
    return category;
  }

  public static ActivityCategory getActivityCategory(Activity activity) {
    return switch (activity) {
      case RUNNING, CYCLING, SWIMMING, WALKING, ROWING, SPINNING -> ActivityCategory.ENDURANCE;
      case YOGA, PILATES -> ActivityCategory.FLEXIBILITY;
      case STRENGTH_TRAINING, HIIT -> ActivityCategory.STRENGTH;
      case FOOTBALL, BASKETBALL -> ActivityCategory.TEAM_SPORT;
      case HIKING, CLIMBING -> ActivityCategory.OUTDOOR;
    };
  }
}
