import { Component, OnInit } from '@angular/core';

import { Md5 } from 'md5-typescript';

@Component({
    selector: 'appfab-contributors',
    templateUrl: './contributors.component.html',
    styleUrls: ['./contributors.component.scss']
})
export class ContributorsComponent implements OnInit {

    authors: Author[];

    constructor() {
    }

    ngOnInit() {

        this.authors = [];

        this.authors.push(
            { name: 'Dave Shanley', email: 'dshanley@vmware.com', hash: Md5.init('dshanley@vmware.com')});

        this.authors.push(
            { name: 'Akmal Khan', email: 'akmalk@vmware.com', hash: Md5.init('akmalk@vmware.com')});

        this.authors.push(
            { name: 'Josh Kim', email: 'kjosh@vmware.com', hash: Md5.init('kjosh@vmware.com')});

        this.authors.push(
            { name: 'Etienne Le Sueur', email: 'elesueur@vmware.com', hash: Md5.init('elesueur@vmware.com')});

        this.authors.push(
            { name: 'Matt Critchlow', email: 'mcritch@vmware.com', hash: Md5.init('mcritch@vmware.com')});

        this.authors.push(
            { name: 'Andrii Alieksashyn', email: 'aalieksashyn@vmware.com', hash: Md5.init('aalieksashyn@vmware.com')});

        this.authors.push(
            { name: 'Jeremy Wilken', email: 'wilkenj@vmware.com', hash: Md5.init('wilkenj@vmware.com')});

        this.authors.push(
            { name: 'Ashwini Manjunath Kanadam', email: 'ashwinim@vmware.com', hash: Md5.init('ashwinim@vmware.com')});

        this.authors.push(
            { name: 'Prathamesh Satpute', email: 'psatpute@vmware.com', hash: Md5.init('psatpute@vmware.com')});

        this.authors.push(
            { name: 'Stoyan Hristov', email: 'shristov@vmware.com', hash: Md5.init('shristov@vmware.com')});

        this.authors.push(
            { name: 'Kevin MacDonell', email: 'kmacdonell@vmware.com', hash: Md5.init('kmacdonell@vmware.com')});

        this.authors.push(
            { name: 'Ruman Hassan', email: 'hruman@vmware.com', hash: Md5.init('hruman@vmware.com')});

        this.authors.push(
            { name: 'Seth Tompkins', email: 'stompkins@vmware.com', hash: Md5.init('stompkins@vmware.com')});

        this.authors.push(
            { name: 'Kevin Buffington', email: 'kbuffington@vmware.com', hash: Md5.init('kbuffington@vmware.com')});

        this.authors.push(
            { name: 'Sandeep Hegde', email: 'hsandeep@vmware.com', hash: Md5.init('hsandeep@vmware.com')});

        this.authors.push(
            { name: 'Emily Chen', email: 'chenemily@vmware.com', hash: Md5.init('chenemily@vmware.com')});

        this.authors.push(
            { name: 'Brian Duncan', email: 'bduncan@vmware.com', hash: Md5.init('bduncan@vmware.com')});

        this.authors.push(
            { name: 'Varun Joshi', email: 'joshivarun@vmware.com', hash: Md5.init('joshivarun@vmware.com')});



        this.authors = this.shuffle(this.authors);

    }

    // taken from https://stackoverflow.com/questions/2450954/how-to-randomize-shuffle-a-javascript-array
    // why is this not built into the standard lib?

    private shuffle(array: Author[]): Author[] {
        var currentIndex = array.length, temporaryValue, randomIndex;

        // While there remain elements to shuffle...
        while (0 !== currentIndex) {

            // Pick a remaining element...
            randomIndex = Math.floor(Math.random() * currentIndex);
            currentIndex -= 1;

            // And swap it with the current element.
            temporaryValue = array[currentIndex];
            array[currentIndex] = array[randomIndex];
            array[randomIndex] = temporaryValue;
        }

        return array;
    }

}

interface Author {
    name: string;
    email: string;
    hash: string;
}
