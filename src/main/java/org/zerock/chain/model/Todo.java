package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "todo")
@ToString
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_no")
    private Long todoNo;

    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "todo_content", columnDefinition = "LONGTEXT")
    private String todoContent;

    @Column(name = "todo_category")
    private String todoCategory;

    @Column(name = "todo_status")
    private boolean todoStatus= false;

    @Column(name = "todo_favorite")
    private boolean todoFavorite = false;

    @Column(name = "todo_created_date")
    private LocalDate todoCreatedDate = LocalDate.now();

    public void setTodoStatus(boolean todoStatus) {
        this.todoStatus = todoStatus;
    }
    public void setTodoFavoriteStatus(boolean todoFavorite) {
        this.todoFavorite = todoFavorite;
    }

}
