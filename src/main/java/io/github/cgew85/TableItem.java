package io.github.cgew85;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by cgew85 on 06.05.2016.
 */

@AllArgsConstructor
public class TableItem {
    @Getter
    @Setter
    private String groupName;

    @Getter
    @Setter
    private String text;
}
