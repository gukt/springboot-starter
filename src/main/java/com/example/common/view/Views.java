package com.example.common.view;

public interface Views {

  interface IdOnly {
  }

  interface WithAttrs extends IdOnly {
  }

  interface Basic extends IdOnly {
  }

  interface Detail extends Basic {
  }

  /**
   * 仅管理员可见
   */
  interface Admin extends Basic {
  }

}
