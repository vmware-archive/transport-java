import { AbstractBase } from '@vmw/bifrost/core';

export abstract class BaseComponent extends AbstractBase {

    protected loggedIn = false;

    constructor(name: string) {
        super(name);
    }
}
