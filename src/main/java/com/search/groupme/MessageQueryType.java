package com.search.groupme;

/**
 * The three query parameters the GroupMe API accepts when querying for messages.
 * More details: https://dev.groupme.com/docs/v3#messages
 *
 * Example:
 * Say we have these message IDs (with 1 being most recent, 5 being the least recent):
 * 1, 2, 3, 4, 5
 *
 * before_id=3 returns 4, 5
 * since_id=3 returns 1, 2
 * after_id=3 returns 2, 1
 */
public enum MessageQueryType {
  BEFORE_ID, // Get messages earlier in time than the ID, descending order
  SINCE_ID,  // Get messages later in time than the ID, descending order
  AFTER_ID   // Get messages later in time than the ID, ascending order
}
