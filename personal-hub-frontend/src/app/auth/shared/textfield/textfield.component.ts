// textfield.component.ts
import { Component, Input, Output, EventEmitter } from "@angular/core";

@Component({
    selector: 'app-textfield',
    templateUrl: './textfield.component.html',
    styleUrls: ['./textfield.component.css']
})

export class TextFieldComponent {
    @Input() type: string = 'text';
    @Input() placeholder: string = '';
    
    // Thêm các phần cần thiết cho two-way binding
    @Input() value: string = '';
    @Output() valueChange = new EventEmitter<string>();
    
    // Phương thức xử lý khi giá trị thay đổi
    onInputChange(event: any) {
        this.value = event.target.value;
        this.valueChange.emit(this.value);
    }
}