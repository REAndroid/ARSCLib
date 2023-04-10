/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.archive2.block;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateBlock extends LengthPrefixedBytes{
    public CertificateBlock() {
        super(false);
    }

    public X509Certificate getCertificate(){
        return generateCertificate(getByteArray().toArray());
    }
    public static X509Certificate generateCertificate(byte[] encodedForm){
        CertificateFactory factory = getCertFactory();
        if(factory == null){
            return null;
        }
        try{
            // TODO: cert bytes could be in DER format ?
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(encodedForm));
        }catch (CertificateException ignored){
            return null;
        }
    }
    private static CertificateFactory getCertFactory() {
        if (sCertFactory == null) {
            try {
                sCertFactory = CertificateFactory.getInstance("X.509");
            } catch (CertificateException ignored) {
            }
        }
        return sCertFactory;
    }

    private static CertificateFactory sCertFactory = null;
}
